package com.schwab.agentic.orchestration;

import com.schwab.agentic.agent.AgentRegistry;
import com.schwab.agentic.domain.*;
import com.schwab.agentic.governance.RetryPolicy;
import com.schwab.agentic.metrics.AuditTrail;
import com.schwab.agentic.policy.PolicyEngine;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowEngineTest {
    @Test void pausesAtHumanGateAndContinuesAfterApproval() {
        var registry = allowAllAgents();
        var engine = new WorkflowEngine(registry, new PolicyEngine(List.of()), new AuditTrail());
        var approval = new WorkflowNode("architecture", AgentType.ARCHITECTURE, Set.of("requirements"), RetryPolicy.none(), true);
        var execution = new WorkflowExecution("e1", List.of(
                new WorkflowNode("requirements", AgentType.REQUIREMENT, Set.of(), RetryPolicy.none(), false), approval,
                new WorkflowNode("implementation", AgentType.IMPLEMENTATION, Set.of("architecture"), RetryPolicy.none(), false)
        ), "build shortener");

        engine.advance(execution);
        assertEquals(WorkflowStatus.WAITING_APPROVAL, execution.status());
        assertEquals(NodeStatus.WAITING_APPROVAL, approval.status());
        engine.approve(execution, "architecture", "human-reviewer");
        assertEquals(WorkflowStatus.COMPLETED, execution.status());
    }

    @Test void boundedRetryCanRecover() {
        var registry = allowAllAgents();
        var count = new AtomicInteger();
        registry.register(AgentType.IMPLEMENTATION, (n,c) -> {
            if (count.incrementAndGet() == 1) throw new RuntimeException("compile failed");
            return "fixed";
        });
        var engine = new WorkflowEngine(registry, new PolicyEngine(List.of()), new AuditTrail());
        var impl = new WorkflowNode("implementation", AgentType.IMPLEMENTATION, Set.of(), RetryPolicy.bounded(2), false);
        var execution = new WorkflowExecution("e2", List.of(impl), "change");
        engine.advance(execution);
        assertEquals(WorkflowStatus.COMPLETED, execution.status());
        assertEquals(2, impl.attempts());
    }

    private AgentRegistry allowAllAgents() {
        var registry = new AgentRegistry();
        for (AgentType type : AgentType.values()) registry.register(type, (n,c) -> type + " output");
        return registry;
    }
}
