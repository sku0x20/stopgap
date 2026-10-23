package dev.sku20.stopgap.app.logger

import dev.sku20.stopgap.ir.annotation.Creates

object LoggerConfig {

    @Creates
    fun loggerEndpoint(): LoggerEndpoint {
        return LoggerEndpoint()
    }

}
