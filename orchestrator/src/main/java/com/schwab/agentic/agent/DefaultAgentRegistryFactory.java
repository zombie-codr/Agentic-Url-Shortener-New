package com.schwab.agentic.agent;

import com.schwab.agentic.agent.impl.BrownfieldCodebaseAnalysisAgent;
import com.schwab.agentic.agent.impl.BrownfieldImplementationAgent;
import com.schwab.agentic.agent.impl.BrownfieldValidationAgent;
import com.schwab.agentic.agent.impl.ImpactPlannerAgent;
import com.schwab.agentic.domain.AgentType;

public final class DefaultAgentRegistryFactory {

    private DefaultAgentRegistryFactory() {
        // Utility class - prevent instantiation
    }

    public static AgentRegistry create() {

        AgentRegistry registry = new AgentRegistry();

        registry.register(
                AgentType.CODEBASE_ANALYSIS,
                new BrownfieldCodebaseAnalysisAgent()
        );

        registry.register(
                AgentType.PLANNER,
                new ImpactPlannerAgent()
        );

        registry.register(
                AgentType.IMPLEMENTATION,
                new BrownfieldImplementationAgent()
        );

        registry.register(
                AgentType.TEST,
                new BrownfieldValidationAgent()
        );

        return registry;
    }
}