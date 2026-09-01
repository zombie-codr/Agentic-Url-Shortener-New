package com.schwab.agentic.agent.output;

import java.util.List;

public record ValidationReport(
        boolean passed,
        List<String> checks,
        List<String> evidence,
        List<String> residualRisks
) {
}