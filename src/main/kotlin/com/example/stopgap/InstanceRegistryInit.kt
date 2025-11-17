package com.example.stopgap

import com.example.stopgap.MainConfig.mainEndpoint
import com.example.stopgap.exception.ExceptionConfig
import com.example.stopgap.generator.GeneratorConfig
import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry

object InstanceRegistryInit {

    lateinit var registry: InstanceRegistry

    fun init(config: Config) {
        registry = InstanceRegistry(config)
        registerCreators()
    }

    private fun registerCreators() {
        registry.registerForType(::mainEndpoint)
        GeneratorConfig.setup(registry)
        ExceptionConfig.setup(registry)
    }

}