package dev.sku20.stopgap.helidon.mapper.plaintext

import dev.sku20.stopgap.helidon.mapper.mediatyperule.MediaTypeRule
import io.helidon.http.HttpMediaType

object PlainTextMediaTypeRule : MediaTypeRule {
    override fun accepts(mediaType: HttpMediaType?): Boolean = TODO()
}
