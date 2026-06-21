package com.example.stopgap.serde

import com.example.stopgap.serde.fastjson.FastjsonSerde
import dev.sku20.ir.Creates

object SerdeConfig {

    @Creates
    fun serdeEndpoint(): SerdeEndpoint {
        return SerdeEndpoint()
    }

    @Creates
    fun fastjsonSerde(): FastjsonSerde {
        return FastjsonSerde()
    }

}