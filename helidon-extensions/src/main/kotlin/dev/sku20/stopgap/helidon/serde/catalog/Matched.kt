package dev.sku20.stopgap.helidon.serde.catalog

import io.helidon.http.HttpMediaType

/** A [SerdeCatalog]'s pick: the [value] and the registered [mediaType] it matched, not the raw request header. */
data class Matched<out T>(
    val mediaType: HttpMediaType,
    val value: T
)
