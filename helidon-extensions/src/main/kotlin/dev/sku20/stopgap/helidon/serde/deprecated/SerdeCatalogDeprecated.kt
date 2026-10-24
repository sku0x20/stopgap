package dev.sku20.stopgap.helidon.serde.deprecated

import io.helidon.http.HttpMediaType

/**
 * Chooses a [SerdeDeprecated] for a request, so serdes never read request headers.
 *
 * Each [SerdeDeprecated] handles one exact media type, charset included. Mapping request media types onto them
 * (which parameters count, defaults when absent, name normalization) is the catalog's job.
 *
 * @see MapSerdeCatalog
 */
interface SerdeCatalogDeprecated {

    /** Resolves a [SerdeDeprecated] to deserialize a request body, e.g. by its Content-Type. */
    fun getDeserializer(mediaType: HttpMediaType?): SerdeDeprecated

    /** Resolves a [SerdeDeprecated] to serialize a response, e.g. by a request's Accept header. */
    fun getSerializer(mediaTypes: List<HttpMediaType>): SerdeDeprecated
}
