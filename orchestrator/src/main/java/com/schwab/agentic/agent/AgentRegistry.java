package com.schwab.agentic.agent;
import com.schwab.agentic.domain.AgentType;
import java.util.*;
public class AgentRegistry {
    private final Map<AgentType, EngineeringAgent> agents = new EnumMap<>(AgentType.class);
    public void register(AgentType type, EngineeringAgent agent) { agents.put(type, agent); }
    public EngineeringAgent get(AgentType type) {
        EngineeringAgent agent = agents.get(type);
        if (agent == null) throw new IllegalStateException("No agent registered for " + type);
        return agent;
    }
}
