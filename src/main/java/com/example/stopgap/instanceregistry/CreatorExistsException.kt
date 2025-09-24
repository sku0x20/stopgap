package com.example.stopgap.instanceregistry;

public class CreatorExistsException extends RuntimeException {
    public CreatorExistsException(String qualifier) {
        super("creator already exists for qualifier: " + qualifier);
    }
}
