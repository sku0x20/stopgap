package com.example.stopgap.exception

import dev.sku20.ir.Creates
import dev.sku20.ir.InstanceRegistry

object ExceptionConfig {

    @Creates
    fun exceptionEndpoint(registry: InstanceRegistry): ExceptionEndpoint {
        return ExceptionEndpoint()
    }

}
