package com.example.stopgap.exception

import dev.sku20.ir.InstanceRegistry

object ExceptionConfig {

    fun exceptionEndpoint(registry: InstanceRegistry): ExceptionEndpoint {
        return ExceptionEndpoint()
    }

}
