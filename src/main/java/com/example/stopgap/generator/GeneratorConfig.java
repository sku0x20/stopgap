package com.example.stopgap.generator;

import com.example.stopgap.generator.uuid.UuidConfig;
import com.example.stopgap.generator.web.GeneratorEndpoint;
import com.example.stopgap.instanceregistry.InstanceRegistry;

public final class GeneratorConfig {
    private GeneratorConfig() {
    }

    public static void setup(final InstanceRegistry registry) {
        registry.registerForType(GeneratorEndpoint.class, GeneratorConfig::generator);
        registry.registerForType(StaticGenerator.class, GeneratorConfig::staticGenerator);
        UuidConfig.setup(registry);
    }

    private static GeneratorEndpoint generator(final InstanceRegistry registry) {
        final var staticGenerator = registry.getInstanceForType(StaticGenerator.class);
        return new GeneratorEndpoint(staticGenerator);
    }

    private static StaticGenerator staticGenerator(final InstanceRegistry registry) {
//        final var config = registry.getConfig();
//        final var string = config.get("static");
        return new StaticGenerator("static");
    }

}
