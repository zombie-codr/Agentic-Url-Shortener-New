package com.schwab.agentic.agent.impl;

import com.schwab.agentic.agent.EngineeringAgent;
import com.schwab.agentic.agent.output.BrownfieldImpactAnalysis;
import com.schwab.agentic.agent.output.ChangePlan;
import com.schwab.agentic.context.EngineeringContext;
import com.schwab.agentic.orchestration.WorkflowNode;

import java.util.List;

public class ImpactPlannerAgent implements EngineeringAgent {

    @Override
    public Object execute(
            WorkflowNode node,
            EngineeringContext context
    ) {

        Object analysisOutput = context.output("codebase-analysis");

        if (!(analysisOutput instanceof BrownfieldImpactAnalysis analysis)) {
            throw new IllegalStateException(
                    "Brownfield impact analysis is required before impact planning"
            );
        }

        List<ChangePlan.ChangeTask> tasks =
                analysis.impactedArtifacts()
                        .stream()
                        .map(artifact ->
                                new ChangePlan.ChangeTask(
                                        artifact.path(),
                                        artifact.changeType().name(),
                                        artifact.risk().name(),
                                        artifact.reason(),
                                        artifact.dependencies()
                                )
                        )
                        .toList();

        List<String> regressionConstraints =
                analysis.regressionRisks();

        return new ChangePlan(
                tasks,
                regressionConstraints
        );
    }
}