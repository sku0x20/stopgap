package com.example.stopgap.generator.uuid;

import com.example.stopgap.generator.uuid.web.UuidEndpoint;
import com.example.stopgap.instanceregistry.InstanceRegistry;

public final class UuidConfig {
    private UuidConfig() {
    }

    public static void setup(final InstanceRegistry registry) {
        registry.registerForType(UuidEndpoint.class, UuidConfig::endpoint);
        registry.registerForType(UuidGen.class, UuidConfig::gen);
    }

    private static UuidEndpoint endpoint(final InstanceRegistry registry) {
        final var uuidGen = registry.getInstanceForType(UuidGen.class);
        return new UuidEndpoint(uuidGen);
    }

    private static UuidGen gen(final InstanceRegistry registry) {
        return new UuidGen();
    }

}
