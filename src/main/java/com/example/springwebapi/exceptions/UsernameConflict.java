package com.example.springwebapi.exceptions;

public class UsernameConflict extends org.springframework.security.core.AuthenticationException {
    public UsernameConflict(String message) {
        super(message);
    }

    public UsernameConflict(String message, Throwable cause) {
        super(message, cause);
    }

}
