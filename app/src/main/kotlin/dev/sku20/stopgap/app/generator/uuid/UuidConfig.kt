package dev.sku20.stopgap.app.generator.uuid

import dev.sku20.stopgap.app.generator.uuid.web.UuidEndpoint
import dev.sku20.stopgap.ir.Creates
import dev.sku20.stopgap.ir.Qualifier

object UuidConfig {

    @Creates
    fun uuidEndpoint(
        @Qualifier("uuidGen.v4")
        uuidGen: UuidGen
    ): UuidEndpoint {
        return UuidEndpoint(uuidGen)
    }

    @Creates
    @Qualifier("uuidGen.v4")
    fun uuidGen(): UuidGen {
        return UuidGen()
    }
}
