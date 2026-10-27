package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.InMemoryPartContent
import io.helidon.http.ContentDisposition
import io.helidon.http.HttpMediaType
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class PartReader(
    boundary: String,
    bodyStream: InputStream
) {

    private val dashBoundary = "--$boundary".toByteArray(Charsets.UTF_8)
    private val delimiter = "\r\n--$boundary".toByteArray(Charsets.UTF_8)
    private val bufferedInputStream = BufferedInputStream(bodyStream)
    private var isClosed: Boolean

    // Only the opening boundary is read here; later ones are consumed with the delimiter that ends each part.
    init {
        val boundaryBytes = bufferedInputStream.readNBytes(dashBoundary.size)
        if (!boundaryBytes.contentEquals(dashBoundary)) throw MalformedBodyException(null)
        isClosed = readIsClosed()
    }

    fun hasNext(): Boolean = !isClosed

    fun part(): Part {
        if (isClosed) throw NoSuchElementException()
        val headers = readHeaders()
        val content = readUntil(delimiter)
        val part = toPart(headers, content)
        isClosed = readIsClosed()
        return part
    }

    // "--" after a boundary closes the body; CRLF means a part follows.
    private fun readIsClosed(): Boolean {
        val nextBytes = bufferedInputStream.readNBytes(2)
        if (nextBytes.contentEquals(DASHES)) return true
        if (!nextBytes.contentEquals(CRLF)) throw MalformedBodyException(null)
        return false
    }

    private fun toPart(headers: Map<String, String>, content: ByteArray): Part {
        val dispositionHeader = headers["content-disposition"] ?: throw MalformedBodyException(null)
        // TODO: Helidon rejects filename="", which RFC 6266 allows and browsers send for an empty file input.
        val disposition = ContentDisposition.parse(dispositionHeader)
        val name = disposition.contentName().orElse(null) ?: throw MalformedBodyException(null)
        val filename = disposition.filename().orElse(null)
        val type = contentType(headers["content-type"] ?: DEFAULT_CONTENT_TYPE)
        return Part(name, filename, type, InMemoryPartContent(content))
    }

    private fun contentType(value: String): HttpMediaType {
        try {
            return HttpMediaType.create(value)
        } catch (e: IllegalArgumentException) {
            throw MalformedBodyException(e)
        }
    }

    // Header names are lowercased, since they are case-insensitive.
    private fun readHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        while (true) {
            val line = readLine()
            if (line.isEmpty()) return headers
            val colon = line.indexOf(':')
            if (colon < 0) throw MalformedBodyException(null)
            headers[line.substring(0, colon).trim().lowercase()] = line.substring(colon + 1).trim()
        }
    }

    private fun readLine(): String = String(readUntil(CRLF), Charsets.UTF_8)

    // Returns the bytes before sequence and consumes sequence itself.
    // On a mismatch, matching restarts at 0, or at 1 if the byte is CR. That is enough only because CR is
    // the sequence's first byte and appears nowhere else in it, so a partial match can't hide another start.
    private fun readUntil(sequence: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        var matched = 0
        while (matched < sequence.size) {
            val read = bufferedInputStream.read()
            if (read < 0) throw MalformedBodyException(null)
            val byte = read.toByte()
            if (byte == sequence[matched]) {
                matched++
                continue
            }
            // The bytes matched so far were content after all.
            output.write(sequence, 0, matched)
            if (byte == sequence[0]) {
                matched = 1
            } else {
                matched = 0
                output.write(read)
            }
        }
        return output.toByteArray()
    }

    private companion object {
        const val DEFAULT_CONTENT_TYPE = "text/plain"
        val DASHES = "--".toByteArray(Charsets.UTF_8)
        val CRLF = "\r\n".toByteArray(Charsets.UTF_8)
    }
}
