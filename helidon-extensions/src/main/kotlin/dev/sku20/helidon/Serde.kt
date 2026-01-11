package dev.sku20.helidon

import kotlin.reflect.KClass

interface Serde {
    fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T
    fun <T : Any> serialize(obj: T): ByteArray
}