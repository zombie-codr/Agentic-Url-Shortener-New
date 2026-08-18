package com.schwab.agentic.metrics;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
public class AuditTrail {
    private final List<WorkflowEvent> events = new CopyOnWriteArrayList<>();
    public void record(WorkflowEvent event) { events.add(event); }
    public List<WorkflowEvent> events() { return List.copyOf(events); }
}
