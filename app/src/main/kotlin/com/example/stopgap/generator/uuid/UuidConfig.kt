package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import dev.sku20.ir.Creates

object UuidConfig {

    @Creates
    fun uuidEndpoint(
        @Qualifier("uuidGen.v4")
        uuidGen: UuidGen
    ): UuidEndpoint {
        return UuidEndpoint(uuidGen)
    }

    @Creates
    fun uuidGen(): UuidGen {
        return UuidGen()
    }
}
