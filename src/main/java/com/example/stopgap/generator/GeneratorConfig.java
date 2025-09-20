package com.example.stopgap.generator;

import com.example.stopgap.generator.web.GeneratorEndpoint;
import com.example.stopgap.instanceregistry.InstanceRegistry;

public final class GeneratorConfig {
    private GeneratorConfig() {
    }

    public static void setup(final InstanceRegistry registry) {
        registry.registerForType(GeneratorEndpoint.class, GeneratorConfig::generator);
    }

    private static GeneratorEndpoint generator(final InstanceRegistry registry) {
        return new GeneratorEndpoint();
    }

}
