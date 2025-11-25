package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry

object UuidConfig {

    fun uuidEndpoint(registry: InstanceRegistry): UuidEndpoint {
        val uuidGen = registry.getInstanceForType<UuidGen>()
        return UuidEndpoint(uuidGen)
    }

    fun uuidGen(registry: InstanceRegistry): UuidGen {
        return UuidGen()
    }
}
