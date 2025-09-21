package com.example.stopgap.exception;

import com.example.stopgap.instanceregistry.InstanceRegistry;

public final class ExceptionConfig {
    private ExceptionConfig() {
    }

    public static void setup(final InstanceRegistry registry) {
        registry.registerForType(ExceptionEndpoint.class, ExceptionConfig::exceptionEndpoint);
    }

    private static ExceptionEndpoint exceptionEndpoint(final InstanceRegistry registry) {
        return new ExceptionEndpoint();
    }
}
