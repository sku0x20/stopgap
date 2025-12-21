package dev.sku20.ir

fun interface InstanceCreator<T> {
    fun create(registry: InstanceRegistry): T
}
