package com.schwab.shortener.api;

import com.schwab.shortener.exception.ShortCodeConflictException;
import com.schwab.shortener.exception.UrlExpiredException;
import com.schwab.shortener.exception.UrlNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "BAD_REQUEST",
                        ex.getMessage(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(ShortCodeConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ShortCodeConflictException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "SHORT_CODE_CONFLICT",
                        ex.getMessage(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            UrlNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "URL_NOT_FOUND",
                        ex.getMessage(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(
            UrlExpiredException ex) {

        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(new ErrorResponse(
                        "URL_EXPIRED",
                        ex.getMessage(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "DATA_CONFLICT",
                        "The requested short code already exists",
                        Instant.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred",
                        Instant.now()
                ));
    }

    public record ErrorResponse(
            String code,
            String message,
            Instant timestamp
    ) {}
}