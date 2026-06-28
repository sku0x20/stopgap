package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * Resolves a [Serde] by media type. Implementations choose the selection strategy.
 *
 * @see MapSerdeCatalog
 */
interface SerdeCatalog {

    /** Resolves a [Serde] to deserialize a request body, e.g. by its Content-Type. */
    fun getDeserializer(mediaType: HttpMediaType?): Serde

    /** Resolves a [Serde] to serialize a response, e.g. by a request's Accept header. */
    fun getSerializer(mediaTypes: List<HttpMediaType>): Serde
}
