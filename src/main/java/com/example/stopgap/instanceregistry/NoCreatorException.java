package com.example.stopgap.instanceregistry;

public class NoCreatorException extends RuntimeException {
    public NoCreatorException(String qualifier) {
        super("no creator registered for qualifier: " + qualifier);
    }
}
