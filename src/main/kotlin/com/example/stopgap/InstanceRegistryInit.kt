package com.example.stopgap

import com.example.stopgap.MainConfig.mainEndpoint
import com.example.stopgap.exception.ExceptionConfig.exceptionEndpoint
import com.example.stopgap.generator.GeneratorConfig.generator
import com.example.stopgap.generator.GeneratorConfig.staticGenerator
import com.example.stopgap.generator.uuid.UuidConfig.endpoint
import com.example.stopgap.generator.uuid.UuidConfig.gen
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
        registry.registerForType(::generator)
        registry.registerForType(::staticGenerator)
        registry.registerForType(::exceptionEndpoint)
        registry.registerForType(::endpoint)
        registry.registerForType(::gen)
    }

}