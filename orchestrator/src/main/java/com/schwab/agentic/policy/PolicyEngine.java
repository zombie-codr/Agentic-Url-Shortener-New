package com.schwab.agentic.policy;
import com.schwab.agentic.context.EngineeringContext;
import com.schwab.agentic.orchestration.WorkflowNode;
import java.util.List;
public class PolicyEngine {
    private final List<EngineeringPolicy> policies;
    public PolicyEngine(List<EngineeringPolicy> policies) { this.policies = List.copyOf(policies); }
    public void validate(WorkflowNode node, Object output, EngineeringContext context) {
        for (EngineeringPolicy policy : policies) {
            var result = policy.evaluate(node, output, context);
            if (!result.allowed()) throw new PolicyViolationException(result.reason());
        }
    }
    public static class PolicyViolationException extends RuntimeException { public PolicyViolationException(String message) { super(message); } }
}
