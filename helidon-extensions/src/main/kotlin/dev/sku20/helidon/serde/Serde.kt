package dev.sku20.helidon.serde

import kotlin.reflect.KClass
import kotlin.reflect.KType

// T: Any ensures non-nullable types only; KClass<T> carries type info lost to JVM erasure.
// KType overload supports generic types (e.g. List<Foo>); no T : Any needed as KType encodes nullability.
interface Serde {
    fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T
    fun <T> deserialize(bytes: ByteArray, type: KType): T
    fun <T : Any> serialize(obj: T): ByteArray
}