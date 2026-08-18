package com.schwab.shortener.exception;

public class ShortCodeConflictException extends RuntimeException {

    public ShortCodeConflictException(String code) {
        super("Short code already exists: " + code);
    }
}
