package com.example.stopgap

import com.example.stopgap.exception.ExceptionConfig
import com.example.stopgap.generator.GeneratorConfig
import com.example.stopgap.instanceregistry.InstanceRegistry

object MainConfig {

    fun setup(registry: InstanceRegistry) {
        registry.registerForType(::mainEndpoint)
        GeneratorConfig.setup(registry)
        ExceptionConfig.setup(registry)
    }

    private fun mainEndpoint(registry: InstanceRegistry): MainEndpoint {
        return MainEndpoint()
    }

}
