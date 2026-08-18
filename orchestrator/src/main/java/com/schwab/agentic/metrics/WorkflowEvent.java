package com.schwab.agentic.metrics;
import java.time.Instant;
public record WorkflowEvent(String executionId, String nodeId, String eventType, int attempt, Instant timestamp, String detail) {}
