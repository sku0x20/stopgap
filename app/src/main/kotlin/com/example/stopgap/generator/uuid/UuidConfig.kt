package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import dev.sku20.ir.InstanceRegistry

object UuidConfig {

    fun uuidEndpoint(registry: InstanceRegistry): UuidEndpoint {
        val uuidGen = registry.getInstanceForType<UuidGen>()
        return UuidEndpoint(uuidGen)
    }

    fun uuidGen(registry: InstanceRegistry): UuidGen {
        return UuidGen()
    }
}
