package com.schwab.agentic.governance;
import java.time.Duration;
public record RetryPolicy(int maxAttempts, Duration backoff) {
    public static RetryPolicy none() { return new RetryPolicy(1, Duration.ZERO); }
    public static RetryPolicy bounded(int maxAttempts) { return new RetryPolicy(maxAttempts, Duration.ZERO); }
}
