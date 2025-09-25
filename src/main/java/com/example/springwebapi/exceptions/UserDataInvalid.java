package com.example.springwebapi.exceptions;

public class UserDataInvalid extends RuntimeException {
    public UserDataInvalid(String message) {
        super(message);
    }

    public UserDataInvalid(String message, Throwable cause) {
        super(message, cause);
    }
}
