package dev.sku20.stopgap.app.exception

import dev.sku20.stopgap.ir.Creates

object ExceptionConfig {

    @Creates
    fun exceptionEndpoint(): ExceptionEndpoint {
        return ExceptionEndpoint()
    }

}
