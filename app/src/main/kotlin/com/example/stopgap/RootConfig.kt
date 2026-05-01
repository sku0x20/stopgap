package com.example.stopgap

import com.example.stopgap.serde.fastjson.FastjsonSerde
import dev.sku20.helidon.serde.Serde
import dev.sku20.helidon.serde.SerdeExtras
import dev.sku20.ir.Creates
import dev.sku20.ir.Qualifier
import io.helidon.config.Config

object RootConfig {

    @Creates
    fun config(): Config {
        val config = Config.create()
        return config
    }

    @Creates
    @Qualifier(SerdeExtras.DEFAULT_QUALIFIER)
    fun defaultSerde(): Serde = FastjsonSerde()

    @Creates
    fun rootEndpoint(): RootEndpoint {
        return RootEndpoint()
    }

    @Creates
    fun httpMethodsEndpoint(): HttpMethodsEndpoint {
        return HttpMethodsEndpoint()
    }

}
