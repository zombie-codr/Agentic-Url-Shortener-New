package com.schwab.agentic.agent;
import com.schwab.agentic.context.EngineeringContext;
import com.schwab.agentic.orchestration.WorkflowNode;
public interface EngineeringAgent { Object execute(WorkflowNode node, EngineeringContext context); }
