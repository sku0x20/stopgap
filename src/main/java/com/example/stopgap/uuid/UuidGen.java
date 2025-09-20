package com.example.stopgap.uuid;

// todo: make it non static

public final class UuidGen {

    private UuidGen() {
    }

    public static String generate() {
        return java.util.UUID.randomUUID().toString();
    }
}
