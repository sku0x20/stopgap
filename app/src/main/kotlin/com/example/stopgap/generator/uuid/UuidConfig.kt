package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import dev.sku20.ir.Creates
import dev.sku20.ir.InstanceRegistry

object UuidConfig {

    @Creates
    fun uuidEndpoint(registry: InstanceRegistry): UuidEndpoint {
        val uuidGen = registry.getInstanceForType<UuidGen>()
        return UuidEndpoint(uuidGen)
    }

    @Creates
    fun uuidGen(): UuidGen {
        return UuidGen()
    }
}
