package com.schwab.shortener.service;

import com.schwab.shortener.api.UrlDtos.*;
import com.schwab.shortener.domain.ShortUrl;
import com.schwab.shortener.exception.ShortCodeConflictException;
import com.schwab.shortener.exception.UrlExpiredException;
import com.schwab.shortener.exception.UrlNotFoundException;
import com.schwab.shortener.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class UrlShortenerService {
    private final ShortUrlRepository repository;
    private final Clock clock;

    UrlShortenerService(ShortUrlRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ShortUrl create(CreateUrlRequest request) {
        validateUrl(request.url());
        Instant now = clock.instant();
        if (request.expiresAt() != null && !request.expiresAt().isAfter(now)) {
            throw new IllegalArgumentException("expiresAt must be in the future");
        }
        String code = request.customAlias() == null || request.customAlias().isBlank()
                ? generateCode() : normalizeAlias(request.customAlias());
        if (repository.existsByShortCode(code)) {
            throw new ShortCodeConflictException(code);
        }

        return repository.save(new ShortUrl(code, request.url(), now, request.expiresAt()));
    }

    @Transactional
    public String resolve(String shortCode) {
        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        Instant now = clock.instant();
        if (shortUrl.isExpired(now)) throw new UrlExpiredException(shortCode);
        repository.incrementClickCount(shortCode, now);
        return shortUrl.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse analytics(String shortCode) {
        ShortUrl u = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        return new AnalyticsResponse(u.getShortCode(), u.getClickCount(), u.getLastAccessedAt(), u.getCreatedAt());
    }

    private void validateUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!(scheme.equals("http") || scheme.equals("https")) || uri.getHost() == null) {
                throw new IllegalArgumentException("Only absolute HTTP/HTTPS URLs are allowed");
            }
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException iae) throw iae;
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }

    private String normalizeAlias(String alias) {
        if (!alias.matches("[A-Za-z0-9_-]{4,32}")) throw new IllegalArgumentException("Invalid customAlias");
        return alias;
    }

    private String generateCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
    }
}
