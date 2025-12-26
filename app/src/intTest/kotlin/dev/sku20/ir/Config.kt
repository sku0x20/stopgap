package dev.sku20.ir

object EagerlyConfig {

    @Creates(eagerly = true)
    fun eagerly(registry: InstanceRegistry): Eagerly {
        return Eagerly()
    }

    class Eagerly {
        val creationTime = System.nanoTime()
    }

}