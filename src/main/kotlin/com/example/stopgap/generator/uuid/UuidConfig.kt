package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry

object UuidConfig {

    fun endpoint(registry: InstanceRegistry): UuidEndpoint {
        val uuidGen = registry.getInstanceForType<UuidGen>()
        return UuidEndpoint(uuidGen)
    }

    fun gen(registry: InstanceRegistry): UuidGen {
        return UuidGen()
    }
}
