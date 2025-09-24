package com.example.stopgap.generator

import com.example.stopgap.generator.uuid.UuidConfig
import com.example.stopgap.generator.web.GeneratorEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry

object GeneratorConfig {

    fun setup(registry: InstanceRegistry) {
        registry.registerForType(
            GeneratorEndpoint::class.java,
            ::generator
        )
        registry.registerForType(
            StaticGenerator::class.java,
            ::staticGenerator
        )
        UuidConfig.setup(registry)
    }

    private fun generator(registry: InstanceRegistry): GeneratorEndpoint {
        val staticGenerator = registry.getInstanceForType(StaticGenerator::class.java)
        return GeneratorEndpoint(staticGenerator)
    }

    private fun staticGenerator(registry: InstanceRegistry): StaticGenerator {
        val config = registry.config
        val staticValue = config.get("generator.static.value")
        return StaticGenerator(staticValue)
    }
}
