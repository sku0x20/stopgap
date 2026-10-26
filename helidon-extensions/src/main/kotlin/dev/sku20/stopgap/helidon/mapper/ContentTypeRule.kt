package dev.sku20.stopgap.helidon.mapper

import io.helidon.http.HttpMediaType

interface ContentTypeRule {
    fun accepts(contentType: HttpMediaType?): Boolean
}
