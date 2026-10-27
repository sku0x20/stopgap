package dev.sku20.stopgap.helidon.mapper.req.multipart

import io.helidon.http.HttpMediaType

data class UploadedFile(
    val fieldName: String,
    val fileName: String,
    val contentType: HttpMediaType,
    val size: Long,
)
