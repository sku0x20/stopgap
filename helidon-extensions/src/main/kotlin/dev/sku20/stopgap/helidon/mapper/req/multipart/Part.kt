package dev.sku20.stopgap.helidon.mapper.req.multipart

import io.helidon.http.HttpMediaType
import java.io.InputStream

data class Part(
    val name: String,
    val fileName: String?,
    val contentType: HttpMediaType,
    private val body: PartBody,
) {
    /** Opens a new stream over the part's content on every call. */
    fun inputStream(): InputStream = body.inputStream()
}
