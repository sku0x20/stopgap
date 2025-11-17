package com.example.stopgap

import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry

object InstanceRegistryInit {

    lateinit var registry: InstanceRegistry

    fun init(config: Config) {
        registry = InstanceRegistry(config)
    }

}