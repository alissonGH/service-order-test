package com.example.exception;

public class InternalException extends RuntimeException {

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalException(String message) {
        super(message);
    }
}
