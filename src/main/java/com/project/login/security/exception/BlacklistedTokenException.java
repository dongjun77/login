package com.project.login.security.exception;

public class BlacklistedTokenException extends RuntimeException {

    public BlacklistedTokenException(String message) {
        super(message);
    }

}