package com.example.stopgap

import com.example.stopgap.instanceregistry.InstanceRegistry

object MainConfig {

    fun config(registry: InstanceRegistry): Config {
        val config = HelidonConfig.loadDefault()
        return config
    }

    fun mainEndpoint(registry: InstanceRegistry): MainEndpoint {
        return MainEndpoint()
    }

}
