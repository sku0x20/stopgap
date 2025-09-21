package com.example.stopgap.exception;

public class BadClientException extends RuntimeException {
    public BadClientException() {
        super("Bad client request");
    }
}
