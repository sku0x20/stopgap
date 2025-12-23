package com.example.stopgap.generator

import com.example.stopgap.generator.web.GeneratorEndpoint
import dev.sku20.ir.Creates
import dev.sku20.ir.InstanceRegistry
import io.helidon.config.Config

object GeneratorConfig {

    @Creates
    fun generatorEndpoint(registry: InstanceRegistry): GeneratorEndpoint {
        val staticGenerator = registry.getInstanceForType<StaticGenerator>()
        return GeneratorEndpoint(staticGenerator)
    }

    @Creates
    fun staticGenerator(registry: InstanceRegistry): StaticGenerator {
        val config = registry.getInstanceForType<Config>()
        val staticValue = config.get("generator.static.value").asString().get()
        return StaticGenerator(staticValue)
    }
}
