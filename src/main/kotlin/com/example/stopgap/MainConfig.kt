package com.example.stopgap

import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.config.Config

object MainConfig {

    fun config(registry: InstanceRegistry): Config {
        val config = Config.create()
        return config
    }

    fun mainEndpoint(registry: InstanceRegistry): MainEndpoint {
        return MainEndpoint()
    }

}
