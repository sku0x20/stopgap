package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * Resolves a [Serde] for a given media type.
 *
 * Implementations are free to choose how that resolution happens, e.g. a lookup
 * keyed by media type, a switch over known types, or always returning the same
 * [Serde] regardless of the requested media type. Implement this interface directly
 * for any custom selection strategy.
 *
 * @param mediaType the requested media type, or `null` if the request didn't specify one
 * (e.g. a missing Content-Type/Accept header)
 * @see MapSerdeCatalog
 */
interface SerdeCatalog {
    // TODO: once Serde is split into separate Serializer/Deserializer interfaces,
    // split this into getSerializer(accept)/getDeserializer(contentType) too.
    fun get(mediaType: HttpMediaType?): Serde
}
