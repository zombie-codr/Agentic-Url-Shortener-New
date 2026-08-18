package com.schwab.agentic.policy;
import com.schwab.agentic.context.EngineeringContext;
import com.schwab.agentic.orchestration.WorkflowNode;
public interface EngineeringPolicy {
    PolicyResult evaluate(WorkflowNode node, Object output, EngineeringContext context);
    record PolicyResult(boolean allowed, String reason) {
        public static PolicyResult allow() { return new PolicyResult(true, "allowed"); }
        public static PolicyResult block(String reason) { return new PolicyResult(false, reason); }
    }
}
