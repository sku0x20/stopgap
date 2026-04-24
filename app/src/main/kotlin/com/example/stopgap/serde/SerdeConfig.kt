package com.example.stopgap.serde

import dev.sku20.ir.Creates

object SerdeConfig {

    @Creates
    fun serdeEndpoint(): SerdeEndpoint {
        return SerdeEndpoint()
    }

}