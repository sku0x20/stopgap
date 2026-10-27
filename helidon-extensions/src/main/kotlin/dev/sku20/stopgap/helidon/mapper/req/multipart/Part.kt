package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.multipart.partcontent.PartContent
import io.helidon.http.HttpMediaType

data class Part(
    val name: String,
    val filename: String?,
    val type: HttpMediaType,
    val content: PartContent,
)