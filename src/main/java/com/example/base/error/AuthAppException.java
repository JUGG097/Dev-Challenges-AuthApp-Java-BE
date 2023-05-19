package com.example.base.error;

public class AuthAppException extends Exception{
    public AuthAppException () { super(); };

    public AuthAppException(String message) {
        super(message);
    }
}
