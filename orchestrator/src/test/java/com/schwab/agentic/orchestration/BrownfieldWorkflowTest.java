package com.schwab.agentic.orchestration;

import com.schwab.agentic.agent.AgentRegistry;
import com.schwab.agentic.domain.AgentType;
import com.schwab.agentic.agent.DefaultAgentRegistryFactory;
import com.schwab.agentic.agent.output.BrownfieldImpactAnalysis;
import com.schwab.agentic.agent.output.ChangePlan;
import com.schwab.agentic.agent.output.ImplementationProposal;
import com.schwab.agentic.agent.output.ValidationReport;
import com.schwab.agentic.domain.NodeStatus;
import com.schwab.agentic.domain.WorkflowStatus;
import com.schwab.agentic.metrics.AuditTrail;
import com.schwab.agentic.scenario.ScenarioFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BrownfieldWorkflowTest {

    @Test
    void brownfieldWorkflowShouldProduceStructuredEngineeringEvidence() {

        AgentRegistry registry =
                DefaultAgentRegistryFactory.create();

        AuditTrail auditTrail =
                new AuditTrail();

        WorkflowEngine engine =
                new WorkflowEngine(
                        registry,
                        new com.schwab.agentic.policy.PolicyEngine(List.of()),
                        auditTrail
                );

        WorkflowExecution execution =
                ScenarioFactory.brownfield(
                        "Add expiration support to the existing URL shortener"
                );

        engine.advance(execution);

        /*
         * 1. Verify codebase analysis was produced.
         */
        Object analysisOutput =
                execution.context()
                        .output("codebase-analysis");

        assertNotNull(analysisOutput);

        assertInstanceOf(
                BrownfieldImpactAnalysis.class,
                analysisOutput
        );

        BrownfieldImpactAnalysis analysis =
                (BrownfieldImpactAnalysis) analysisOutput;

        /*
         * 2. Verify impacted artifacts were identified.
         */
        assertFalse(
                analysis.impactedArtifacts().isEmpty()
        );

        assertTrue(
                analysis.impactedArtifacts()
                        .stream()
                        .anyMatch(artifact ->
                                artifact.path()
                                        .contains("ShortUrl.java")
                        )
        );

        assertTrue(
                analysis.impactedArtifacts()
                        .stream()
                        .anyMatch(artifact ->
                                artifact.path()
                                        .contains("UrlShortenerService.java")
                        )
        );

        /*
         * 3. Verify the agent also identifies artifacts
         * that do NOT need modification.
         */
        assertFalse(
                analysis.unaffectedArtifacts().isEmpty()
        );

        assertTrue(
                analysis.unaffectedArtifacts()
                        .stream()
                        .anyMatch(artifact ->
                                artifact.path()
                                        .contains("findByShortCode")
                        )
        );

        /*
         * 4. Verify brownfield regression reasoning.
         */
        assertTrue(
                analysis.regressionRisks()
                        .stream()
                        .anyMatch(risk ->
                                risk.toLowerCase()
                                        .contains(
                                                "must not increment click analytics"
                                        )
                        )
        );

        /*
         * 5. Verify the planner consumed the analysis
         * and produced a ChangePlan.
         */
        Object planOutput =
                execution.context()
                        .output("impact-plan");

        assertNotNull(planOutput);

        assertInstanceOf(
                ChangePlan.class,
                planOutput
        );

        ChangePlan changePlan =
                (ChangePlan) planOutput;

        assertFalse(
                changePlan.tasks().isEmpty()
        );

        assertTrue(
                changePlan.tasks()
                        .stream()
                        .anyMatch(task ->
                                task.path()
                                        .contains("UrlShortenerService.java")
                        )
        );

        /*
         * 6. Verify implementation proposal was produced.
         */
        Object implementationOutput =
                execution.context()
                        .output("implementation");

        assertNotNull(implementationOutput);

        assertInstanceOf(
                ImplementationProposal.class,
                implementationOutput
        );

        ImplementationProposal implementation =
                (ImplementationProposal) implementationOutput;

        assertFalse(
                implementation.changes().isEmpty()
        );

        assertTrue(
                implementation.changes()
                        .stream()
                        .anyMatch(change ->
                                change.path()
                                        .contains("GlobalExceptionHandler.java")
                        )
        );

        /*
         * 7. Verify preservation of existing behavior.
         */
        assertTrue(
                implementation.preservedBehaviors()
                        .stream()
                        .anyMatch(behavior ->
                                behavior.contains("404")
                        )
        );

        /*
         * 8. Verify validation agent produced a report.
         */
        Object validationOutput =
                execution.context()
                        .output("validation");

        assertNotNull(validationOutput);

        assertInstanceOf(
                ValidationReport.class,
                validationOutput
        );

        ValidationReport validation =
                (ValidationReport) validationOutput;

        assertTrue(
                validation.passed()
        );

        assertFalse(
                validation.checks().isEmpty()
        );

        assertFalse(
                validation.evidence().isEmpty()
        );

        assertFalse(
                validation.residualRisks().isEmpty()
        );

        /*
         * 9. Verify workflow pauses at the release
         * approval gate instead of releasing automatically.
         */
        assertEquals(
                WorkflowStatus.WAITING_APPROVAL,
                execution.status()
        );

        assertEquals(
                NodeStatus.WAITING_APPROVAL,
                execution.node("release").status()
        );

        /*
         * 10. Verify audit trail contains meaningful
         * workflow evidence.
         */
        assertTrue(
                auditTrail.events()
                        .stream()
                        .anyMatch(event ->
                                event.nodeId()
                                        .equals("codebase-analysis")
                                        &&
                                        event.eventType()
                                                .equals("SUCCEEDED")
                        )
        );

        assertTrue(
                auditTrail.events()
                        .stream()
                        .anyMatch(event ->
                                event.nodeId()
                                        .equals("release")
                                        &&
                                        event.eventType()
                                                .equals("APPROVAL_REQUIRED")
                        )
        );
    }
}