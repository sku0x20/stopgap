package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import java.io.InputStream

class PartReader(
    boundary: String,
    private val bodyStream: InputStream
) {

    private val closeDelimiter = "--$boundary--".toByteArray(Charsets.UTF_8)

    fun part(): Part? {
        val bytes = bodyStream.readNBytes(closeDelimiter.size)
        if (bytes.contentEquals(closeDelimiter)) return null
        TODO()
    }
}
