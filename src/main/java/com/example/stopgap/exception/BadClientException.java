package com.example.stopgap.exception;

import io.helidon.http.HttpException;
import io.helidon.http.Status;

public class BadClientException extends HttpException {
    public BadClientException() {
        super("Bad client request by extending", Status.BAD_REQUEST_400);
    }
}
