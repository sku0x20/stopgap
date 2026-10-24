package dev.sku20.stopgap.helidon.serde

import kotlin.reflect.KType

/** Converts bytes to objects, unaware of media types, e.g. a `JsonUtf8Serde` for UTF-8 JSON. */
interface Deserializer {
    // KType, not KClass: it keeps type arguments (e.g. Wrapper<Foo>) and nullability that erasure drops.
    // Callers should cache it; typeOf() per call is ~4x slower (see FastjsonDeserializeBenchmark).
    fun <T> deserialize(bytes: ByteArray, type: KType): T
}
