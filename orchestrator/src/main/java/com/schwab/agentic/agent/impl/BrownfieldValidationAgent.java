package com.schwab.agentic.agent.impl;

import com.schwab.agentic.agent.EngineeringAgent;
import com.schwab.agentic.agent.output.BrownfieldImpactAnalysis;
import com.schwab.agentic.agent.output.ImplementationProposal;
import com.schwab.agentic.agent.output.ValidationReport;
import com.schwab.agentic.context.EngineeringContext;
import com.schwab.agentic.orchestration.WorkflowNode;

import java.util.ArrayList;
import java.util.List;

public class BrownfieldValidationAgent implements EngineeringAgent {

    @Override
    public Object execute(
            WorkflowNode node,
            EngineeringContext context
    ) {

        BrownfieldImpactAnalysis analysis =
                getImpactAnalysis(context);

        ImplementationProposal implementation =
                getImplementationProposal(context);

        List<String> checks = new ArrayList<>();
        List<String> evidence = new ArrayList<>();
        List<String> residualRisks = new ArrayList<>();

        boolean serviceChangePresent =
                implementation.changes()
                        .stream()
                        .anyMatch(change ->
                                change.path()
                                        .contains("UrlShortenerService.java")
                        );

        checks.add(
                "Implementation includes the redirect-service change required for expiration."
        );

        if (serviceChangePresent) {
            evidence.add(
                    "ImplementationProposal contains a change for UrlShortenerService.java."
            );
        }

        boolean exceptionHandlingPresent =
                implementation.changes()
                        .stream()
                        .anyMatch(change ->
                                change.path()
                                        .contains("UrlExpiredException.java")
                        );

        checks.add(
                "Implementation includes a dedicated expired-URL domain failure."
        );

        if (exceptionHandlingPresent) {
            evidence.add(
                    "ImplementationProposal contains UrlExpiredException.java."
            );
        }

        boolean http410MappingPresent =
                implementation.changes()
                        .stream()
                        .anyMatch(change ->
                                change.path()
                                        .contains("GlobalExceptionHandler.java")
                        );

        checks.add(
                "Expired URLs are mapped through the API exception layer."
        );

        if (http410MappingPresent) {
            evidence.add(
                    "ImplementationProposal contains GlobalExceptionHandler.java."
            );
        }

        boolean expirationTestsIdentified =
                analysis.requiredTests()
                        .stream()
                        .anyMatch(test ->
                                test.toLowerCase()
                                        .contains("expired url")
                        );

        checks.add(
                "Brownfield analysis identified expiration-specific tests."
        );

        if (expirationTestsIdentified) {
            evidence.add(
                    "Impact analysis includes required tests for expired URL behavior."
            );
        }

        boolean analyticsRegressionProtected =
                analysis.regressionRisks()
                        .stream()
                        .anyMatch(risk ->
                                risk.toLowerCase()
                                        .contains("must not increment click analytics")
                        );

        checks.add(
                "Expired redirects must not increment analytics."
        );

        if (analyticsRegressionProtected) {
            evidence.add(
                    "Impact analysis explicitly protects analytics from expired redirects."
            );
        }

        boolean preservedExistingBehavior =
                implementation.preservedBehaviors()
                        .stream()
                        .anyMatch(behavior ->
                                behavior.toLowerCase()
                                        .contains("404")
                        );

        checks.add(
                "Existing missing-short-code behavior is preserved."
        );

        if (preservedExistingBehavior) {
            evidence.add(
                    "ImplementationProposal preserves existing HTTP 404 behavior."
            );
        }

        boolean passed =
                serviceChangePresent
                        && exceptionHandlingPresent
                        && http410MappingPresent
                        && expirationTestsIdentified
                        && analyticsRegressionProtected
                        && preservedExistingBehavior;

        residualRisks.add(
                "This validation checks structured engineering artifacts but does not yet execute Maven tests."
        );

        residualRisks.add(
                "Database migration behavior against PostgreSQL is not validated by this agent."
        );

        residualRisks.add(
                "Concurrent redirect behavior is not load-tested by this validation stage."
        );

        return new ValidationReport(
                passed,
                List.copyOf(checks),
                List.copyOf(evidence),
                List.copyOf(residualRisks)
        );
    }

    private BrownfieldImpactAnalysis getImpactAnalysis(
            EngineeringContext context
    ) {

        Object output =
                context.output("codebase-analysis");

        if (!(output instanceof BrownfieldImpactAnalysis analysis)) {
            throw new IllegalStateException(
                    "Brownfield impact analysis is required before validation"
            );
        }

        return analysis;
    }

    private ImplementationProposal getImplementationProposal(
            EngineeringContext context
    ) {

        Object output =
                context.output("implementation");

        if (!(output instanceof ImplementationProposal implementation)) {
            throw new IllegalStateException(
                    "Implementation proposal is required before validation"
            );
        }

        return implementation;
    }
}