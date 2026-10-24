package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType

/** Converts objects to bytes of its [mediaType], e.g. a `JsonUtf8Serde` for UTF-8 JSON. */
interface Serializer {
    /** Format and charset it writes; a serializer can't exist without one. */
    val mediaType: HttpMediaType

    fun <T> serialize(obj: T): ByteArray
}
