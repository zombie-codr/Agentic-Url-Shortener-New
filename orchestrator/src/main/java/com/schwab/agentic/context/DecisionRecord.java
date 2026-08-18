package com.schwab.agentic.context;
import java.time.Instant;
import java.util.List;
public record DecisionRecord(String stage, String decision, String rationale, List<String> alternatives, Instant timestamp, String approvedBy) {}
