package com.example.stopgap.generators;

import com.example.stopgap.generators.web.GeneratorEndpoint;
import com.example.stopgap.instanceregistry.InstanceRegistry;

public final class GeneratorIRSetup {
    private GeneratorIRSetup() {
    }

    public static void setup(final InstanceRegistry registry) {
        registry.registerForType(GeneratorEndpoint.class, GeneratorIRSetup::generator);
    }

    private static GeneratorEndpoint generator(final InstanceRegistry registry) {
        return new GeneratorEndpoint();
    }

}
