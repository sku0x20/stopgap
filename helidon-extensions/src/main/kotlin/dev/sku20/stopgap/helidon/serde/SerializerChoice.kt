package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType

/** A [SerdeCatalog]'s pick: the [serializer] to use and the [mediaType] it was chosen for. */
data class SerializerChoice(
    val mediaType: HttpMediaType,
    val serializer: Serializer
)
