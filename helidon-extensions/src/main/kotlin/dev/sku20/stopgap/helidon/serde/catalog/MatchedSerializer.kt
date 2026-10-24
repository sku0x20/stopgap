package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Serializer
import io.helidon.http.HttpMediaType

/** A [SerdeCatalog]'s pick: the [serializer] and the registered [mediaType] it matched, not the raw Accept header. */
data class MatchedSerializer(
    val mediaType: HttpMediaType,
    val serializer: Serializer
)
