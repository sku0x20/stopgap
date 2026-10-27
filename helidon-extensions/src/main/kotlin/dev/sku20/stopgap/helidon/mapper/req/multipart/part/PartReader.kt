package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import java.io.InputStream

class PartReader(
    boundary: String,
    private val bodyStream: InputStream
) {

    private val dashBoundary = "--$boundary".toByteArray(Charsets.UTF_8)

    fun part(): Part? {
        val boundaryBytes = bodyStream.readNBytes(dashBoundary.size)
        if (!boundaryBytes.contentEquals(dashBoundary)) throw MalformedBodyException(null)
        // "--" closes the body; CRLF means a part follows.
        val nextBytes = bodyStream.readNBytes(2)
        if (nextBytes.contentEquals(DASHES)) return null
        if (!nextBytes.contentEquals(CRLF)) throw MalformedBodyException(null)
        TODO("read headers")
    }

    private companion object {
        val DASHES = "--".toByteArray(Charsets.UTF_8)
        val CRLF = "\r\n".toByteArray(Charsets.UTF_8)
    }
}
