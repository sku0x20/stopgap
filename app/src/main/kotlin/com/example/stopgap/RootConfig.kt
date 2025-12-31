package com.example.stopgap

import dev.sku20.ir.Creates
import dev.sku20.ir.InstanceRegistry
import io.helidon.config.Config

object RootConfig {

    @Creates
    fun config(registry: InstanceRegistry): Config {
        val config = Config.create()
        return config
    }

    @Creates
    fun rootEndpoint(registry: InstanceRegistry): RootEndpoint {
        return RootEndpoint()
    }

    @Creates
    fun httpMethodsEndpoint(registry: InstanceRegistry): HttpMethodsEndpoint {
        return HttpMethodsEndpoint()
    }

}
