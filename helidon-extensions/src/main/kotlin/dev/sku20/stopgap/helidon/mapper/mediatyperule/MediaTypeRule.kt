package dev.sku20.stopgap.helidon.mapper.mediatyperule

import io.helidon.http.HttpMediaType

interface MediaTypeRule {
    fun accepts(mediaType: HttpMediaType?): Boolean
}
