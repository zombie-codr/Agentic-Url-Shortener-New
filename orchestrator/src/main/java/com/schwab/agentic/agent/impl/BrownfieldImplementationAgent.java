package com.schwab.agentic.agent.impl;

import com.schwab.agentic.agent.EngineeringAgent;
import com.schwab.agentic.agent.output.ChangePlan;
import com.schwab.agentic.agent.output.ImplementationProposal;
import com.schwab.agentic.context.EngineeringContext;
import com.schwab.agentic.orchestration.WorkflowNode;

import java.util.List;

public class BrownfieldImplementationAgent implements EngineeringAgent {

    @Override
    public Object execute(
            WorkflowNode node,
            EngineeringContext context
    ) {

        Object planOutput =
                context.output("impact-plan");

        if (!(planOutput instanceof ChangePlan changePlan)) {
            throw new IllegalStateException(
                    "Change plan is required before implementation"
            );
        }

        List<ImplementationProposal.FileChange> changes =
                changePlan.tasks()
                        .stream()
                        .map(task ->
                                new ImplementationProposal.FileChange(
                                        task.path(),
                                        task.changeType(),
                                        buildSummary(task),
                                        task.reason()
                                )
                        )
                        .toList();

        return new ImplementationProposal(
                changes,
                List.of(
                        "Existing URLs without expiration must continue to work.",
                        "Existing 404 behavior for missing short codes must remain unchanged.",
                        "Active URLs must continue to increment analytics.",
                        "Existing custom alias behavior must remain unchanged."
                )
        );
    }

    private String buildSummary(ChangePlan.ChangeTask task) {

        String path = task.path();

        if (path.contains("ShortUrl.java")) {
            return "Add expiration state and domain-level expiration behavior.";
        }

        if (path.contains("UrlDtos.java")) {
            return "Extend the create URL request contract with optional expiresAt.";
        }

        if (path.contains("UrlShortenerService.java")) {
            return "Validate expiration during creation and reject expired URLs before analytics are incremented.";
        }

        if (path.contains("UrlExpiredException.java")) {
            return "Add a dedicated domain exception for expired short URLs.";
        }

        if (path.contains("GlobalExceptionHandler.java")) {
            return "Map expired short URLs to HTTP 410 Gone.";
        }

        if (path.contains("UrlShortenerServiceTest.java")) {
            return "Add regression and expiration-specific service tests.";
        }

        return "Apply the planned brownfield change for this artifact.";
    }
}