package com.example.stopgap;

import com.example.stopgap.generator.GeneratorConfig;
import com.example.stopgap.instanceregistry.InstanceRegistry;

final class MainConfig {
    private MainConfig() {
    }

    static void setup(final InstanceRegistry registry) {
        registry.registerForType(MainEndpoint.class, MainConfig::mainEndpoint);
        GeneratorConfig.setup(registry);
    }

    private static MainEndpoint mainEndpoint(final InstanceRegistry registry) {
        return new MainEndpoint();
    }
}
