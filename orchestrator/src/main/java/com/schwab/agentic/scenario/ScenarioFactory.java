package com.schwab.agentic.scenario;

import com.schwab.agentic.domain.AgentType;
import com.schwab.agentic.governance.RetryPolicy;
import com.schwab.agentic.orchestration.*;
import java.util.*;

public final class ScenarioFactory {
    private ScenarioFactory() {}

    public static WorkflowExecution greenfield(String requirement) {
        return new WorkflowExecution(UUID.randomUUID().toString(), List.of(
                node("requirements", AgentType.REQUIREMENT, Set.of(), 1, false),
                node("plan", AgentType.PLANNER, Set.of("requirements"), 1, false),
                node("architecture", AgentType.ARCHITECTURE, Set.of("requirements"), 1, true),
                node("implementation", AgentType.IMPLEMENTATION, Set.of("architecture", "plan"), 2, false),
                node("test-design", AgentType.TEST, Set.of("architecture"), 1, false),
                node("validation", AgentType.TEST, Set.of("implementation", "test-design"), 2, false),
                node("security", AgentType.SECURITY, Set.of("validation"), 1, false),
                node("documentation", AgentType.DOCUMENTATION, Set.of("implementation"), 1, false),
                node("release", AgentType.RELEASE, Set.of("security", "documentation"), 1, true)
        ), requirement);
    }

    public static WorkflowExecution brownfield(String requirement) {
        return new WorkflowExecution(UUID.randomUUID().toString(), List.of(
                node("requirements", AgentType.REQUIREMENT, Set.of(), 1, false),
                node("codebase-analysis", AgentType.CODEBASE_ANALYSIS, Set.of("requirements"), 1, false),
                node("impact-plan", AgentType.PLANNER, Set.of("codebase-analysis"), 1, false),
                node("implementation", AgentType.IMPLEMENTATION, Set.of("impact-plan"), 2, false),
                node("validation", AgentType.TEST, Set.of("implementation"), 2, false),
                node("security", AgentType.SECURITY, Set.of("validation"), 1, false),
                node("release", AgentType.RELEASE, Set.of("security"), 1, true)
        ), requirement);
    }

    public static WorkflowExecution ambiguous(String requirement) {
        return new WorkflowExecution(UUID.randomUUID().toString(), List.of(
                node("requirements", AgentType.REQUIREMENT, Set.of(), 1, true),
                node("privacy-review", AgentType.SECURITY, Set.of("requirements"), 1, true),
                node("plan", AgentType.PLANNER, Set.of("privacy-review"), 1, false),
                node("implementation", AgentType.IMPLEMENTATION, Set.of("plan"), 2, false),
                node("validation", AgentType.TEST, Set.of("implementation"), 2, false),
                node("release", AgentType.RELEASE, Set.of("validation"), 1, true)
        ), requirement);
    }

    private static WorkflowNode node(String id, AgentType type, Set<String> deps, int attempts, boolean approval) {
        return new WorkflowNode(id, type, deps, RetryPolicy.bounded(attempts), approval);
    }
}
