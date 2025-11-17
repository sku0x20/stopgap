package com.example.stopgap

import com.example.stopgap.instanceregistry.InstanceRegistry

object MainConfig {

    fun mainEndpoint(registry: InstanceRegistry): MainEndpoint {
        return MainEndpoint()
    }

}
