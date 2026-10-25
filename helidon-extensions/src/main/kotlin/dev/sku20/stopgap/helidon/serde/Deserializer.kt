package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType
import kotlin.reflect.KType

/** Converts bytes to objects, e.g. a `JsonUtf8Serde` for UTF-8 JSON. */
interface Deserializer {
    /** Whether it reads [mediaType]; it owns the matching rules, e.g. JSON ignores charset. */
    fun canDeserialize(mediaType: HttpMediaType): Boolean

    // KType, not KClass: it keeps type arguments (e.g. Wrapper<Foo>) and nullability that erasure drops.
    // Callers should cache it; typeOf() per call is ~4x slower (see FastjsonDeserializeBenchmark).
    fun <T> deserialize(bytes: ByteArray, type: KType): T
}
