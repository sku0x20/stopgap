package dev.sku20.helidon.serde

import kotlin.reflect.KClass

// T: Any ensures non-nullable types only; KClass<T> carries type info lost to JVM erasure.
interface Serde {
    fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T
    fun <T : Any> serialize(obj: T): ByteArray
}