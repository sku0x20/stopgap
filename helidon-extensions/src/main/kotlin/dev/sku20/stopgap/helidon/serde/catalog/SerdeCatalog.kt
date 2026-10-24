package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.Serializer
import io.helidon.http.ServerRequestHeaders

/**
 * Chooses a [Deserializer] or [Serializer] for a request.
 *
 * Binds serdes to media types: which parameters count (e.g. `charset` for `text/plain`), defaults when absent,
 * and name normalization are the catalog's job.
 */
interface SerdeCatalog {

    /** Picks a [Deserializer] for a request body, e.g. by its Content-Type. */
    fun getDeserializer(headers: ServerRequestHeaders): Matched<Deserializer>

    /** Picks a [Serializer] for a response, e.g. by Accept; [Matched.mediaType] is the response Content-Type. */
    fun getSerializer(headers: ServerRequestHeaders): Matched<Serializer>
}
