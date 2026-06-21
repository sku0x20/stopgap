package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType

interface SerdeCatalog {
    fun get(mediaType: HttpMediaType): Serde?
}
