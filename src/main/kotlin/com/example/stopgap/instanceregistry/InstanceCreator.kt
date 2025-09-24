package com.example.stopgap.instanceregistry

fun interface InstanceCreator<T> {
    fun create(registry: InstanceRegistry): T
}
