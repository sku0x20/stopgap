package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.PartContent
import io.helidon.http.HttpMediaType

data class Part(
    val name: String,
    val filename: String?,
    val type: HttpMediaType,
    val content: PartContent,
) : AutoCloseable {

    override fun close() {
        content.close()
    }
}
