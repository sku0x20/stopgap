package dev.sku20.stopgap.helidon.mapper.req.multipart

import io.helidon.http.HttpMediaType
import java.io.InputStream

class MultiPart(
    val parts: List<Part>,
) {
    class Part(
        val name: String,
        val filename: String?,
        val type: HttpMediaType,
        val content: Content,
    ) {
        class Content(
            val size: Long,
            val inputStream: InputStream,
        )
    }
}


