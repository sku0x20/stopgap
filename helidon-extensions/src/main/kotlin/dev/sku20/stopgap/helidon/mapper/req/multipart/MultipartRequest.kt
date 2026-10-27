package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.MissingBoundaryException
import io.helidon.http.HttpMediaType
import java.io.InputStream

class MultipartRequest(
    private val mediaType: HttpMediaType,
    private val bodyStream: InputStream
) {

    private lateinit var boundary: String

    fun multiPart(): MultiPart {
        parseBoundary()
        return MultiPart(emptyList())
    }

    private fun parseBoundary() {
        val value = mediaType.parameters()["boundary"]
        if (value.isNullOrEmpty()) throw MissingBoundaryException()
        boundary = value
    }
}