package com.schwab.shortener.service;

import com.schwab.shortener.api.UrlDtos.CreateUrlRequest;
import com.schwab.shortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {
    @Mock ShortUrlRepository repository;
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-16T20:00:00Z"), ZoneOffset.UTC);

    @Test void rejectsNonHttpUrls() {
        var service = new UrlShortenerService(repository, clock);
        assertThrows(IllegalArgumentException.class, () -> service.create(new CreateUrlRequest("javascript:alert(1)", null, null)));
    }

    @Test void rejectsExpiredExpiryTime() {
        var service = new UrlShortenerService(repository, clock);
        assertThrows(IllegalArgumentException.class, () -> service.create(new CreateUrlRequest("https://example.com", null, Instant.parse("2026-08-15T00:00:00Z"))));
    }
}
