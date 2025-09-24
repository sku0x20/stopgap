package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry

object UuidConfig {

    fun setup(registry: InstanceRegistry) {
        registry.registerForType(::endpoint)
        registry.registerForType(::gen)
    }

    private fun endpoint(registry: InstanceRegistry): UuidEndpoint {
        val uuidGen = registry.getInstanceForType<UuidGen>()
        return UuidEndpoint(uuidGen)
    }

    private fun gen(registry: InstanceRegistry): UuidGen {
        return UuidGen()
    }
}
