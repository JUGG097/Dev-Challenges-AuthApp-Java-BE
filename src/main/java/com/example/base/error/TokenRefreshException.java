package com.example.base.error;

public class TokenRefreshException extends RuntimeException{
    public TokenRefreshException () { super(); };

    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }
}
