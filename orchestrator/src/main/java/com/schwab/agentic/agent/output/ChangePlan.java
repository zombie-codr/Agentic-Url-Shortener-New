package com.schwab.agentic.agent.output;

import java.util.List;

public record ChangePlan(
        List<ChangeTask> tasks,
        List<String> regressionConstraints
) {

    public record ChangeTask(
            String path,
            String changeType,
            String risk,
            String reason,
            List<String> dependencies
    ) {}
}