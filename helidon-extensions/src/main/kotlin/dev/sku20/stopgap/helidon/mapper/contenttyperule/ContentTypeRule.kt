package dev.sku20.stopgap.helidon.mapper.contenttyperule

import io.helidon.http.HttpMediaType

interface ContentTypeRule {
    fun accepts(contentType: HttpMediaType?): Boolean
}
