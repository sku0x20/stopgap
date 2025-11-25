package com.example.stopgap.exception

import com.example.stopgap.instanceregistry.InstanceRegistry

object ExceptionConfig {

    fun exceptionEndpoint(registry: InstanceRegistry): ExceptionEndpoint {
        return ExceptionEndpoint()
    }

}
