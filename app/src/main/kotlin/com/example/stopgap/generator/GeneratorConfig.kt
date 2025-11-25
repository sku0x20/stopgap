package com.example.stopgap.generator

import com.example.stopgap.generator.web.GeneratorEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.config.Config

object GeneratorConfig {

    fun generatorEndpoint(registry: InstanceRegistry): GeneratorEndpoint {
        val staticGenerator = registry.getInstanceForType<StaticGenerator>()
        return GeneratorEndpoint(staticGenerator)
    }

    fun staticGenerator(registry: InstanceRegistry): StaticGenerator {
        val config = registry.getInstanceForType<Config>()
        val staticValue = config.get("generator.static.value").asString().get()
        return StaticGenerator(staticValue)
    }
}
