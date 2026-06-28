package dev.sku20.stopgap.app

import dev.sku20.stopgap.app.serde.fastjson.FastjsonSerde
import dev.sku20.stopgap.helidon.serde.MapSerdeCatalog
import dev.sku20.stopgap.helidon.serde.SerdeCatalog
import dev.sku20.stopgap.helidon.serde.SerdeExtras
import dev.sku20.stopgap.ir.Creates
import dev.sku20.stopgap.ir.Qualifier
import io.helidon.config.Config

object RootConfig {

    @Creates
    fun config(): Config {
        val config = Config.create()
        return config
    }

    @Creates
    @Qualifier(SerdeExtras.DEFAULT_CATALOG_QUALIFIER)
    fun defaultSerdeCatalog(
        fastjsonSerde: FastjsonSerde
    ): SerdeCatalog {
        val mapSerdeCatalog = MapSerdeCatalog()
        mapSerdeCatalog.add(fastjsonSerde)
        return mapSerdeCatalog
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
