package com.example.stopgap.generator.uuid;

public final class UuidGen {

    public UuidGen() {
    }

    @SuppressWarnings("MethodMayBeStatic")
    public String generate() {
        return java.util.UUID.randomUUID().toString();
    }
}
