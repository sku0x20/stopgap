package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType

/** A [SerdeCatalog]'s pick: the [serde] to use and the [mediaType] it was chosen for. */
data class SerdeChoice(
    val mediaType: HttpMediaType,
    val serde: SerdeDeprecated
)
