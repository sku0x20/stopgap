package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.mediatyperule

import io.helidon.http.HttpMediaType

interface MediaTypeRule {
    fun accepts(mediaType: HttpMediaType?): Boolean
}
