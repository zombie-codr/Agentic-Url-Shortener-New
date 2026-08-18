package com.schwab.shortener.api;

import com.schwab.shortener.api.UrlDtos.*;
import com.schwab.shortener.domain.ShortUrl;
import com.schwab.shortener.exception.UrlExpiredException;
import com.schwab.shortener.exception.UrlNotFoundException;
import com.schwab.shortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
public class UrlController {
    private final UrlShortenerService service;
    public UrlController(UrlShortenerService service) { this.service = service; }

    @PostMapping("/api/v1/urls")
    public ResponseEntity<CreateUrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
        ShortUrl u = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUrlResponse(
                u.getShortCode(), "http://localhost:8080/" + u.getShortCode(), u.getOriginalUrl(), u.getCreatedAt(), u.getExpiresAt()));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(service.resolve(shortCode))).build();
    }

    @GetMapping("/api/v1/urls/{shortCode}/analytics")
    public AnalyticsResponse analytics(@PathVariable String shortCode) { return service.analytics(shortCode); }

    @ExceptionHandler(UrlNotFoundException.class)
    ResponseEntity<Void> notFound() { return ResponseEntity.notFound().build(); }

    @ExceptionHandler(UrlExpiredException.class)
    ResponseEntity<Void> expired() { return ResponseEntity.status(HttpStatus.GONE).build(); }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<String> badRequest(IllegalArgumentException ex) { return ResponseEntity.badRequest().body(ex.getMessage()); }
}
