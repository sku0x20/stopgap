package dev.sku20.stopgap.helidon.serde

/** Converts objects to bytes, unaware of media types, e.g. a `JsonUtf8Serde` for UTF-8 JSON. */
interface Serializer {
    fun <T> serialize(obj: T): ByteArray
}
