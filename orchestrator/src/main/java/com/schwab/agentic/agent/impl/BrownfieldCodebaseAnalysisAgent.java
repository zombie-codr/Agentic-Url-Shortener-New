package com.schwab.agentic.agent.impl;

import com.schwab.agentic.agent.EngineeringAgent;
import com.schwab.agentic.agent.output.BrownfieldImpactAnalysis;
import com.schwab.agentic.context.EngineeringContext;
import com.schwab.agentic.orchestration.WorkflowNode;

import java.util.List;

import static com.schwab.agentic.agent.output.BrownfieldImpactAnalysis.ChangeType.ADD;
import static com.schwab.agentic.agent.output.BrownfieldImpactAnalysis.ChangeType.MODIFY;
import static com.schwab.agentic.agent.output.BrownfieldImpactAnalysis.RiskLevel.HIGH;
import static com.schwab.agentic.agent.output.BrownfieldImpactAnalysis.RiskLevel.LOW;
import static com.schwab.agentic.agent.output.BrownfieldImpactAnalysis.RiskLevel.MEDIUM;

public class BrownfieldCodebaseAnalysisAgent implements EngineeringAgent {

    @Override
    public Object execute(
            WorkflowNode node,
            EngineeringContext context
    ) {

        String requirement = context.originalRequirement();

        return new BrownfieldImpactAnalysis(
                requirement,

                List.of(
                        new BrownfieldImpactAnalysis.ImpactedArtifact(
                                "url-shortener/src/main/java/com/schwab/shortener/domain/ShortUrl.java",
                                MODIFY,
                                MEDIUM,
                                "Expiration must be persisted as part of the short URL lifecycle.",
                                List.of("database schema")
                        ),

                        new BrownfieldImpactAnalysis.ImpactedArtifact(
                                "url-shortener/src/main/java/com/schwab/shortener/api/UrlDtos.java",
                                MODIFY,
                                LOW,
                                "CreateUrlRequest must accept an optional expiration timestamp.",
                                List.of("ShortUrl.java")
                        ),

                        new BrownfieldImpactAnalysis.ImpactedArtifact(
                                "url-shortener/src/main/java/com/schwab/shortener/service/UrlShortenerService.java",
                                MODIFY,
                                HIGH,
                                "The redirect path must reject expired URLs before analytics are incremented.",
                                List.of(
                                        "ShortUrl.java",
                                        "UrlExpiredException.java"
                                )
                        ),

                        new BrownfieldImpactAnalysis.ImpactedArtifact(
                                "url-shortener/src/main/java/com/schwab/shortener/service/UrlExpiredException.java",
                                ADD,
                                LOW,
                                "Expiration introduces a new domain failure that should be represented explicitly.",
                                List.of()
                        ),

                        new BrownfieldImpactAnalysis.ImpactedArtifact(
                                "url-shortener/src/main/java/com/schwab/shortener/api/GlobalExceptionHandler.java",
                                MODIFY,
                                LOW,
                                "Expired URLs should map to HTTP 410 Gone.",
                                List.of("UrlExpiredException.java")
                        ),

                        new BrownfieldImpactAnalysis.ImpactedArtifact(
                                "url-shortener/src/test/java/com/schwab/shortener/service/UrlShortenerServiceTest.java",
                                MODIFY,
                                MEDIUM,
                                "Existing and new expiration behaviors require regression coverage.",
                                List.of("UrlShortenerService.java")
                        )
                ),

                List.of(
                        new BrownfieldImpactAnalysis.UnaffectedArtifact(
                                "ShortUrlRepository.findByShortCode()",
                                "The existing short-code lookup is sufficient for expiration behavior."
                        ),

                        new BrownfieldImpactAnalysis.UnaffectedArtifact(
                                "ShortCodeGenerator",
                                "Expiration does not change short-code generation."
                        ),

                        new BrownfieldImpactAnalysis.UnaffectedArtifact(
                                "AnalyticsResponse",
                                "Expiration does not require a change to the existing analytics response contract."
                        )
                ),

                List.of(
                        "GET /{shortCode}",
                        "UrlController.redirect()",
                        "UrlShortenerService.resolve()",
                        "ShortUrlRepository.findByShortCode()",
                        "increment click analytics",
                        "302 redirect"
                ),

                List.of(
                        "GET /{shortCode}",
                        "UrlController.redirect()",
                        "UrlShortenerService.resolve()",
                        "ShortUrlRepository.findByShortCode()",
                        "check expiration",
                        "if expired -> 410 Gone",
                        "if active -> increment click analytics",
                        "302 redirect"
                ),

                List.of(
                        "Existing non-expiring URLs must continue to redirect successfully.",
                        "Expired URLs must not increment click analytics.",
                        "Existing missing-URL behavior must remain HTTP 404.",
                        "Expiration validation must use a consistent clock/timezone.",
                        "Adding an expiration column must remain backward-compatible with existing rows."
                ),

                List.of(
                        "create without expiresAt should still succeed",
                        "create with future expiresAt should succeed",
                        "create with past expiresAt should fail",
                        "non-expired URL should redirect",
                        "expired URL should return 410",
                        "expired URL should not increment analytics",
                        "missing URL should still return 404"
                ),

                List.of(
                        "Existing rows without expiresAt are treated as non-expiring.",
                        "Expiration is optional.",
                        "Expiration is evaluated using UTC."
                )
        );
    }
}