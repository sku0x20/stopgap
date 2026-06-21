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
 * @see MapSerdeCatalog
 */
interface SerdeCatalog {

    /**
     * Resolves a [Serde] to deserialize a request body, e.g. by its Content-Type.
     *
     * @param mediaType the requested media type, or `null` if the request didn't specify one
     * (e.g. a missing Content-Type header)
     */
    fun getDeserializer(mediaType: HttpMediaType?): Serde

    /**
     * Resolves a [Serde] to serialize a response, e.g. by a request's Accept header.
     * Implementations choose how to pick among the given types (e.g. quality, first match).
     *
     * @param mediaTypes the acceptable media types, possibly empty
     */
    fun getSerializer(mediaTypes: List<HttpMediaType>): Serde
}
