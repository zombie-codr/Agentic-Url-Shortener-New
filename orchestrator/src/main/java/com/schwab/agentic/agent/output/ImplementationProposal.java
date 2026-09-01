package com.schwab.agentic.agent.output;

import java.util.List;

public record ImplementationProposal(
        List<FileChange> changes,
        List<String> preservedBehaviors
) {

    public record FileChange(
            String path,
            String changeType,
            String summary,
            String rationale
    ) {}
}