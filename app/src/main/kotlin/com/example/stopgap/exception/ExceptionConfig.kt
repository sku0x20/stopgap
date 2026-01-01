package com.example.stopgap.exception

import dev.sku20.ir.Creates

object ExceptionConfig {

    @Creates
    fun exceptionEndpoint(): ExceptionEndpoint {
        return ExceptionEndpoint()
    }

}
