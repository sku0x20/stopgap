package com.example.stopgap

import com.example.stopgap.exception.ExceptionConfig
import com.example.stopgap.generator.GeneratorConfig
import com.example.stopgap.generator.uuid.UuidConfig
import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry

object InstanceRegistryInit {

    lateinit var registry: InstanceRegistry

    fun init(config: Config) {
        registry = InstanceRegistry(config)
        registerCreators()
    }

    private fun registerCreators() {
        registry.registerForType(MainConfig::mainEndpoint)

        registry.registerForType(GeneratorConfig::generatorEndpoint)
        registry.registerForType(GeneratorConfig::staticGenerator)
        registry.registerForType(ExceptionConfig::exceptionEndpoint)

        registry.registerForType(UuidConfig::uuidEndpoint)
        registry.registerForType(UuidConfig::uuidGen)
    }

}