package com.schwab.agentic.orchestration;

import com.schwab.agentic.agent.AgentRegistry;
import com.schwab.agentic.domain.*;
import com.schwab.agentic.metrics.*;
import com.schwab.agentic.policy.PolicyEngine;
import java.time.Instant;
import java.util.*;

public class WorkflowEngine {
    private final AgentRegistry agents;
    private final PolicyEngine policyEngine;
    private final AuditTrail auditTrail;

    public WorkflowEngine(AgentRegistry agents, PolicyEngine policyEngine, AuditTrail auditTrail) {
        this.agents = agents; this.policyEngine = policyEngine; this.auditTrail = auditTrail;
    }

    public void advance(WorkflowExecution execution) {
        if (execution.status() == WorkflowStatus.COMPLETED || execution.status() == WorkflowStatus.SAFE_STOPPED) return;
        execution.status(WorkflowStatus.RUNNING);
        boolean progressed;
        do {
            progressed = false;
            for (WorkflowNode node : execution.nodes()) {
                if (node.status() == NodeStatus.SUCCEEDED || node.status() == NodeStatus.WAITING_APPROVAL || node.status() == NodeStatus.SAFE_STOPPED) continue;
                if (!dependenciesSucceeded(execution, node)) continue;
                if (node.status() == NodeStatus.FAILED && node.attempts() >= node.retryPolicy().maxAttempts()) {
                    safeStop(execution, node, "Retry budget exhausted"); return;
                }
                executeNode(execution, node);
                progressed = true;
                if (node.status() == NodeStatus.WAITING_APPROVAL) {
                    execution.status(WorkflowStatus.WAITING_APPROVAL); return;
                }
            }
        } while (progressed && execution.nodes().stream().anyMatch(n -> n.status() != NodeStatus.SUCCEEDED));

        if (execution.nodes().stream().allMatch(n -> n.status() == NodeStatus.SUCCEEDED)) execution.status(WorkflowStatus.COMPLETED);
    }

    public void approve(WorkflowExecution execution, String nodeId, String approver) {
        WorkflowNode node = execution.node(nodeId);
        if (node.status() != NodeStatus.WAITING_APPROVAL) throw new IllegalStateException("Node is not awaiting approval");
        node.status(NodeStatus.SUCCEEDED);
        audit(execution, node, "APPROVED", approver);
        advance(execution);
    }

    private void executeNode(WorkflowExecution execution, WorkflowNode node) {
        node.started(); audit(execution, node, "STARTED", null);
        try {
            Object output = agents.get(node.agentType()).execute(node, execution.context());
            policyEngine.validate(node, output, execution.context());
            execution.context().putOutput(node.id(), output);
            if (node.approvalRequired()) {
                node.status(NodeStatus.WAITING_APPROVAL); audit(execution, node, "APPROVAL_REQUIRED", null);
            } else {
                node.status(NodeStatus.SUCCEEDED); audit(execution, node, "SUCCEEDED", null);
            }
        } catch (RuntimeException ex) {
            node.failed(ex.getMessage()); audit(execution, node, "FAILED", ex.getMessage());
        }
    }

    private boolean dependenciesSucceeded(WorkflowExecution execution, WorkflowNode node) {
        return node.dependencies().stream().allMatch(id -> execution.node(id).status() == NodeStatus.SUCCEEDED);
    }

    private void safeStop(WorkflowExecution execution, WorkflowNode node, String reason) {
        node.status(NodeStatus.SAFE_STOPPED); execution.status(WorkflowStatus.SAFE_STOPPED); audit(execution, node, "SAFE_STOP", reason);
    }

    private void audit(WorkflowExecution execution, WorkflowNode node, String event, String detail) {
        auditTrail.record(new WorkflowEvent(execution.id(), node.id(), event, node.attempts(), Instant.now(), detail));
    }
}
