package dev.sku20.stopgap.ir

import dev.sku20.stopgap.ir.annotation.Creates

object EagerlyConfig {

    @Creates(eagerly = true)
    fun eagerly(registry: InstanceRegistry): Eagerly {
        return Eagerly()
    }

    class Eagerly {
        val creationTime = System.nanoTime()
    }

}