package dev.sku20.stopgap.helidon.mapper.req.multipart

import io.helidon.http.HttpMediaType

data class Part(
    val name: String,
    val fileName: String?,
    val contentType: HttpMediaType,
)
