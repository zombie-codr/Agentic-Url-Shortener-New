package com.schwab.agentic.orchestration;

import com.schwab.agentic.context.EngineeringContext;
import com.schwab.agentic.domain.WorkflowStatus;
import java.util.*;

public class WorkflowExecution {
    private final String id;
    private final Map<String, WorkflowNode> nodes;
    private final EngineeringContext context;
    private WorkflowStatus status = WorkflowStatus.CREATED;

    public WorkflowExecution(String id, Collection<WorkflowNode> nodes, String requirement) {
        this.id = id; this.nodes = new LinkedHashMap<>();
        nodes.forEach(n -> this.nodes.put(n.id(), n));
        this.context = new EngineeringContext(id, requirement);
    }
    public String id() { return id; }
    public Collection<WorkflowNode> nodes() { return nodes.values(); }
    public WorkflowNode node(String id) { return Optional.ofNullable(nodes.get(id)).orElseThrow(); }
    public EngineeringContext context() { return context; }
    public WorkflowStatus status() { return status; }
    public void status(WorkflowStatus status) { this.status = status; }
}
