package com.example.stopgap

import dev.sku20.ir.Creates
import dev.sku20.ir.InstanceRegistry
import io.helidon.config.Config

object MainConfig {

    @Creates
    fun config(registry: InstanceRegistry): Config {
        val config = Config.create()
        return config
    }

    fun mainEndpoint(registry: InstanceRegistry): MainEndpoint {
        return MainEndpoint()
    }

}
