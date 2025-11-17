package com.example.stopgap.generator

import com.example.stopgap.generator.web.GeneratorEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry

object GeneratorConfig {

    fun generator(registry: InstanceRegistry): GeneratorEndpoint {
        val staticGenerator = registry.getInstanceForType<StaticGenerator>()
        return GeneratorEndpoint(staticGenerator)
    }

    fun staticGenerator(registry: InstanceRegistry): StaticGenerator {
        val config = registry.config
        val staticValue = config.get("generator.static.value")
        return StaticGenerator(staticValue)
    }
}
