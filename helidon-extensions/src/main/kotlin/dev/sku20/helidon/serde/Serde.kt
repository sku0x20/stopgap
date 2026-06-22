package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType
import kotlin.reflect.KClass
import kotlin.reflect.KType

// Two deserialize overloads to handle JVM type erasure:
// - KClass<T>: fast path for non-generic types; T : Any required because KClass<T?> is invalid.
// - KType: required for generic types (e.g. Wrapper<Foo>) since KClass loses type arguments at runtime;
//   KType encodes full type info including nullability, so T : Any is not needed.
// Caller selects the right overload statically at compile time based on what type info is available.
interface Serde {
    val mediaType: HttpMediaType
    fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T
    fun <T> deserialize(bytes: ByteArray, type: KType): T
    fun <T> serialize(obj: T): ByteArray
}