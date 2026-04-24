package dev.sku20.helidon.serde

import kotlin.reflect.KType

// KType carries full generic type info (e.g. List<Foo>) unlike KClass which loses type args to erasure.
interface SerdeGeneric {
    fun <T : Any> deserialize(bytes: ByteArray, type: KType): T
    fun <T : Any> serialize(obj: T): ByteArray
}
