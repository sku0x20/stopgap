package dev.sku20.stopgap.helidon.mapper.req.multipart

import io.helidon.http.HttpMediaType
import java.io.InputStream

class MultipartRequest(
    val mediaType: HttpMediaType,
    val bodyStream: InputStream
) {
    fun multiPart(): MultiPart {
        TODO()
    }
}