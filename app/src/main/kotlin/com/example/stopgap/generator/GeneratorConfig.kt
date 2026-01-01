package com.example.stopgap.generator

import com.example.stopgap.generator.web.GeneratorEndpoint
import dev.sku20.ir.Creates
import dev.sku20.ir.InstanceRegistry
import io.helidon.config.Config

object GeneratorConfig {

    @Creates
    fun generatorEndpoint(staticGenerator: StaticGenerator): GeneratorEndpoint {
        return GeneratorEndpoint(staticGenerator)
    }

    @Creates
    fun staticGenerator(config: Config): StaticGenerator {
        val staticValue = config.get("generator.static.value").asString().get()
        return StaticGenerator(staticValue)
    }
}
