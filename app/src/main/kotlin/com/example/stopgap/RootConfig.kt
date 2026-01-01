package com.example.stopgap

import dev.sku20.ir.Creates
import io.helidon.config.Config

object RootConfig {

    @Creates
    fun config(): Config {
        val config = Config.create()
        return config
    }

    @Creates
    fun rootEndpoint(): RootEndpoint {
        return RootEndpoint()
    }

    @Creates
    fun httpMethodsEndpoint(): HttpMethodsEndpoint {
        return HttpMethodsEndpoint()
    }

}
