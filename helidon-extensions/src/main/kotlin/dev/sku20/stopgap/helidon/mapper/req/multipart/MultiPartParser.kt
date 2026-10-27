package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import io.helidon.http.ContentDisposition
import io.helidon.http.HttpMediaType

// Every delimiter after the first is preceded by CRLF, which belongs to the delimiter, not to the part's content.
internal class MultiPartParser(
    private val bytes: ByteArray,
    boundary: String,
) {

    private val delimiter = "\r\n--$boundary".toByteArray(Charsets.US_ASCII)
    private var position = 0

    fun parse(): MultiPart {
        skipPreamble()
        val parts = mutableListOf<Part>()
        while (!isCloseDelimiter()) {
            skipLineEnd()
            parts.add(parsePart())
        }
        return MultiPart(parts)
    }

    // The first delimiter may open the body without a leading CRLF.
    private fun skipPreamble() {
        if (startsWith(delimiter, 0, 2)) {
            position = delimiter.size - 2
            return
        }
        val index = indexOf(delimiter, 0)
        if (index < 0) throw MalformedBodyException(null)
        position = index + delimiter.size
    }

    private fun isCloseDelimiter(): Boolean = startsWith(DASHES, position, 0)

    private fun skipLineEnd() {
        if (!startsWith(CRLF, position, 0)) throw MalformedBodyException(null)
        position += CRLF.size
    }

    private fun parsePart(): Part {
        val headers = parseHeaders()
        val contentEnd = indexOf(delimiter, position)
        if (contentEnd < 0) throw MalformedBodyException(null)
        position = contentEnd + delimiter.size
        return toPart(headers)
    }

    private fun parseHeaders(): Map<String, String> {
        if (startsWith(CRLF, position, 0)) {
            position += CRLF.size
            return emptyMap()
        }
        val end = indexOf(HEADERS_END, position)
        if (end < 0) throw MalformedBodyException(null)
        val block = String(bytes, position, end - position, Charsets.UTF_8)
        position = end + HEADERS_END.size
        val headers = HashMap<String, String>()
        for (line in block.split("\r\n")) {
            val colon = line.indexOf(':')
            if (colon < 0) throw MalformedBodyException(null)
            headers[line.substring(0, colon).trim().lowercase()] = line.substring(colon + 1).trim()
        }
        return headers
    }

    private fun toPart(headers: Map<String, String>): Part {
        val disposition = headers["content-disposition"] ?: throw MalformedBodyException(null)
        try {
            val contentDisposition = ContentDisposition.parse(disposition)
            val name = contentDisposition.contentName().orElseThrow { MalformedBodyException(null) }
            val contentType = headers["content-type"]
            return Part(
                name,
                contentDisposition.filename().orElse(null),
                if (contentType == null) DEFAULT_CONTENT_TYPE else HttpMediaType.create(contentType),
            )
        } catch (e: IllegalArgumentException) {
            throw MalformedBodyException(e)
        }
    }

    private fun startsWith(pattern: ByteArray, from: Int, patternStart: Int): Boolean {
        val length = pattern.size - patternStart
        if (from + length > bytes.size) return false
        for (i in 0 until length) {
            if (bytes[from + i] != pattern[patternStart + i]) return false
        }
        return true
    }

    private fun indexOf(pattern: ByteArray, from: Int): Int {
        for (i in from..bytes.size - pattern.size) {
            if (startsWith(pattern, i, 0)) return i
        }
        return -1
    }

    private companion object {
        val CRLF = "\r\n".toByteArray(Charsets.US_ASCII)
        val DASHES = "--".toByteArray(Charsets.US_ASCII)
        val HEADERS_END = "\r\n\r\n".toByteArray(Charsets.US_ASCII)
        val DEFAULT_CONTENT_TYPE: HttpMediaType = HttpMediaType.create("text/plain")
    }
}
