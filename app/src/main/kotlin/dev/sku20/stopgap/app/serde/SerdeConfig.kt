package dev.sku20.stopgap.app.serde

import dev.sku20.stopgap.app.serde.fastjson.FastjsonSerde
import dev.sku20.stopgap.app.serde.plain.PlainTextSerdeCatalog
import dev.sku20.stopgap.ir.Creates

object SerdeConfig {

    @Creates
    fun serdeEndpoint(): SerdeEndpoint {
        return SerdeEndpoint()
    }

    @Creates
    fun customEndpoint(): CustomEndpoint {
        return CustomEndpoint()
    }

    @Creates
    fun fastjsonSerde(): FastjsonSerde {
        return FastjsonSerde()
    }

    @Creates
    fun plainTextSerdeCatalog(): PlainTextSerdeCatalog {
        return PlainTextSerdeCatalog()
    }

}