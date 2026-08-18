package com.schwab.shortener.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public final class UrlDtos {
    private UrlDtos() {}

    public record CreateUrlRequest(@NotBlank String url, @Pattern(regexp = "[A-Za-z0-9_-]{4,32}")String customAlias, Instant expiresAt) {}
    public record CreateUrlResponse(String shortCode, String shortUrl, String originalUrl, Instant createdAt, Instant expiresAt) {}
    public record AnalyticsResponse(String shortCode, long totalClicks, Instant lastAccessedAt, Instant createdAt) {}
}
