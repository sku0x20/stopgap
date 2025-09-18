package com.example.stopgap;

public final class UuidGen {

    private UuidGen() {
    }

    public static String generate() {
        return java.util.UUID.randomUUID().toString();
    }
}
