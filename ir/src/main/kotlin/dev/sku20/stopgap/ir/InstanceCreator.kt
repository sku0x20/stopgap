package dev.sku20.stopgap.ir

fun interface InstanceCreator<T> {
    fun create(registry: InstanceRegistry): T
}
