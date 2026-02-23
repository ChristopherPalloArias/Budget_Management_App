package com.microservice.auth.exception;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
