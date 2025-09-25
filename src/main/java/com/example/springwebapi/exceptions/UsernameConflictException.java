package com.example.springwebapi.exceptions;

public class UsernameConflictException extends org.springframework.security.core.AuthenticationException {
    public UsernameConflictException(String message) {
        super(message);
    }

    public UsernameConflictException(String message, Throwable cause) {
        super(message, cause);
    }

}
