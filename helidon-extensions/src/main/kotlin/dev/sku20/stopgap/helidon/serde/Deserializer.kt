package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType
import kotlin.reflect.KType

/** Converts bytes of its [mediaType] to objects, e.g. a `JsonUtf8Serde` for UTF-8 JSON. */
interface Deserializer {
    /**
     * What it reads. The format is intrinsic to the serde, so it lives here rather than beside it in the catalog;
     * which requests it matches (charset, wildcards) is still the catalog's call.
     */
    val mediaType: HttpMediaType

    // KType, not KClass: it keeps type arguments (e.g. Wrapper<Foo>) and nullability that erasure drops.
    // Callers should cache it; typeOf() per call is ~4x slower (see FastjsonDeserializeBenchmark).
    fun <T> deserialize(bytes: ByteArray, type: KType): T
}
