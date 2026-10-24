package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * Chooses a [Serde] for a request, so serdes never read request headers.
 *
 * Binds serdes to media types: which parameters count (e.g. `charset` for `text/plain`), defaults when absent,
 * and name normalization are the catalog's job.
 */
interface SerdeCatalog {

    /** Picks a [Serde] to deserialize a request body, e.g. by its Content-Type. */
    fun getDeserializer(mediaType: HttpMediaType?): SerdeChoice

    /** Picks a [Serde] to serialize a response, e.g. by Accept; [SerdeChoice.mediaType] is the response Content-Type. */
    fun getSerializer(mediaTypes: List<HttpMediaType>): SerdeChoice
}
