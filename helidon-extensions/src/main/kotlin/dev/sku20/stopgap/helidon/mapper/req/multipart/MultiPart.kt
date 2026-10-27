package dev.sku20.stopgap.helidon.mapper.req.multipart

import io.helidon.http.HttpMediaType
import java.io.InputStream

data class MultiPart(
    val parts: List<Part>,
) {
    data class Part(
        val name: String,
        val filename: String,
        val type: HttpMediaType,
        val content: Content,
    ) {
        data class Content(
            val size: Int,
            val inputStream: InputStream,
        )
    }
}


