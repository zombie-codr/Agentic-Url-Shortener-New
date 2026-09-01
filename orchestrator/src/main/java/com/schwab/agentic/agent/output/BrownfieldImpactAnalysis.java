package com.schwab.agentic.agent.output;

import java.util.List;

public record BrownfieldImpactAnalysis(
        String requirement,
        List<ImpactedArtifact> impactedArtifacts,
        List<UnaffectedArtifact> unaffectedArtifacts,
        List<String> currentDataFlow,
        List<String> proposedDataFlow,
        List<String> regressionRisks,
        List<String> requiredTests,
        List<String> assumptions
) {

    public record ImpactedArtifact(
            String path,
            ChangeType changeType,
            RiskLevel risk,
            String reason,
            List<String> dependencies
    ) {}

    public record UnaffectedArtifact(
            String path,
            String reason
    ) {}

    public enum ChangeType {
        ADD,
        MODIFY,
        DELETE
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }
}