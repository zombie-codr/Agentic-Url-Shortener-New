package com.schwab.agentic.context;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EngineeringContext {
    private final String executionId;
    private final String originalRequirement;
    private final Map<String, Object> outputs = new ConcurrentHashMap<>();
    private final List<DecisionRecord> decisions = Collections.synchronizedList(new ArrayList<>());

    public EngineeringContext(String executionId, String originalRequirement) {
        this.executionId = executionId; this.originalRequirement = originalRequirement;
    }
    public String executionId() { return executionId; }
    public String originalRequirement() { return originalRequirement; }
    public Map<String, Object> outputs() { return Map.copyOf(outputs); }
    public List<DecisionRecord> decisions() { return List.copyOf(decisions); }
    public void putOutput(String nodeId, Object value) { outputs.put(nodeId, value); }
    public Object output(String nodeId) { return outputs.get(nodeId); }
    public void addDecision(DecisionRecord d) { decisions.add(d); }
}
