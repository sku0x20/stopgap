package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * Chooses a [Serde] for a request, so serdes never read request headers.
 *
 * Each [Serde] handles one exact media type, charset included. Mapping request media types onto them
 * (which parameters count, defaults when absent, name normalization) is the catalog's job.
 *
 * @see MapSerdeCatalog
 */
interface SerdeCatalog {

    /** Resolves a [Serde] to deserialize a request body, e.g. by its Content-Type. */
    fun getDeserializer(mediaType: HttpMediaType?): Serde

    /** Resolves a [Serde] to serialize a response, e.g. by a request's Accept header. */
    fun getSerializer(mediaTypes: List<HttpMediaType>): Serde
}
