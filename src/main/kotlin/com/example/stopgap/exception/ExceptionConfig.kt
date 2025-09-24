package com.example.stopgap.exception

import com.example.stopgap.instanceregistry.InstanceRegistry

object ExceptionConfig {

    fun setup(registry: InstanceRegistry) {
        registry.registerForType(
            ExceptionEndpoint::class.java,
            ::exceptionEndpoint
        )
    }

    private fun exceptionEndpoint(registry: InstanceRegistry): ExceptionEndpoint {
        return ExceptionEndpoint()
    }

}
