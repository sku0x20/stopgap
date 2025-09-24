package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry

object UuidConfig {

    fun setup(registry: InstanceRegistry) {
        registry.registerForType(
            UuidEndpoint::class.java,
            ::endpoint
        )
        registry.registerForType(
            UuidGen::class.java,
            ::gen
        )
    }

    private fun endpoint(registry: InstanceRegistry): UuidEndpoint {
        val uuidGen = registry.getInstanceForType(UuidGen::class.java)
        return UuidEndpoint(uuidGen)
    }

    private fun gen(registry: InstanceRegistry): UuidGen {
        return UuidGen()
    }
}
