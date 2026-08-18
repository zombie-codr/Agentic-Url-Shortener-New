package com.schwab.agentic.orchestration;

import com.schwab.agentic.domain.AgentType;
import com.schwab.agentic.domain.NodeStatus;
import com.schwab.agentic.governance.RetryPolicy;
import java.util.*;

public class WorkflowNode {
    private final String id;
    private final AgentType agentType;
    private final Set<String> dependencies;
    private final RetryPolicy retryPolicy;
    private final boolean approvalRequired;
    private NodeStatus status = NodeStatus.PENDING;
    private int attempts;
    private String lastError;

    public WorkflowNode(String id, AgentType agentType, Set<String> dependencies, RetryPolicy retryPolicy, boolean approvalRequired) {
        this.id = id; this.agentType = agentType; this.dependencies = Set.copyOf(dependencies);
        this.retryPolicy = retryPolicy; this.approvalRequired = approvalRequired;
    }
    public String id() { return id; }
    public AgentType agentType() { return agentType; }
    public Set<String> dependencies() { return dependencies; }
    public RetryPolicy retryPolicy() { return retryPolicy; }
    public boolean approvalRequired() { return approvalRequired; }
    public NodeStatus status() { return status; }
    public int attempts() { return attempts; }
    public String lastError() { return lastError; }
    public void status(NodeStatus status) { this.status = status; }
    public void started() { attempts++; status = NodeStatus.RUNNING; }
    public void failed(String error) { lastError = error; status = NodeStatus.FAILED; }
}
