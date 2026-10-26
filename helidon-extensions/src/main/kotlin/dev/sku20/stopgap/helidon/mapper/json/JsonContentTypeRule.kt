package dev.sku20.stopgap.helidon.mapper.json

import dev.sku20.stopgap.helidon.mapper.ContentTypeRule
import io.helidon.http.HttpMediaType

object JsonContentTypeRule : ContentTypeRule {
    override fun accepts(contentType: HttpMediaType?): Boolean = TODO()
}
