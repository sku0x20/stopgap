package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.InMemoryPartContent
import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.PartContent
import io.helidon.http.ContentDisposition
import io.helidon.http.HttpMediaType
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class PartReader(
    boundary: String,
    bodyStream: InputStream,
    private val bufferSize: Int = 16 * 1024,
) {

    private val dashBoundary = "--$boundary".toByteArray(Charsets.UTF_8)
    private val delimiter = "\r\n--$boundary".toByteArray(Charsets.UTF_8)
    private val bufferedInputStream = BufferedInputStream(bodyStream)
    private var isClosed: Boolean

    // Only the opening boundary is read here; later ones are consumed with the delimiter that ends each part.
    init {
        require(bufferSize <= InMemoryPartContent.MAX_SIZE) {
            "bufferSize $bufferSize exceeds the in-memory limit ${InMemoryPartContent.MAX_SIZE}"
        }
        val boundaryBytes = bufferedInputStream.readNBytes(dashBoundary.size)
        if (boundaryBytes.isEmpty()) throw EmptyBodyException()
        if (!boundaryBytes.contentEquals(dashBoundary)) throw MalformedBodyException(null)
        isClosed = readIsClosed()
    }

    fun hasNext(): Boolean = !isClosed

    fun next(): Part {
        if (isClosed) throw NoSuchElementException()
        val headers = readHeaders()
        val content = readContent()
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

    private fun readContent(): PartContent {
        val writer = PartContentWriter(bufferSize)
        // On failure no Part is returned, so nothing else would ever delete the file.
        try {
            readUntil(delimiter) { writer.write(it) }
        } catch (e: Exception) {
            writer.discard()
            throw e
        }
        return writer.content()
    }

    private fun toPart(headers: Map<String, String>, content: PartContent): Part {
        val dispositionHeader = headers["content-disposition"] ?: throw MalformedBodyException(null)
        // TODO: Helidon rejects filename="", which RFC 6266 allows and browsers send for an empty file input.
        val disposition = ContentDisposition.parse(dispositionHeader)
        val name = disposition.contentName().orElse(null) ?: throw MalformedBodyException(null)
        val filename = disposition.filename().orElse(null)
        val type = contentType(headers["content-type"] ?: DEFAULT_CONTENT_TYPE)
        return Part(name, filename, type, content)
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

    private fun readLine(): String {
        val line = ByteArrayOutputStream()
        readUntil(CRLF) { line.write(it) }
        return line.toString(Charsets.UTF_8)
    }

    // Passes each byte before sequence to write and consumes sequence itself.
    // On a mismatch, matching restarts at 0, or at 1 if the byte is CR. That is enough only because CR is
    // the sequence's first byte and appears nowhere else in it, so a partial match can't hide another start.
    private fun readUntil(sequence: ByteArray, write: (Int) -> Unit) {
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
            for (i in 0 until matched) {
                write(sequence[i].toInt())
            }
            if (byte == sequence[0]) {
                matched = 1
            } else {
                matched = 0
                write(read)
            }
        }
    }

    private companion object {
        const val DEFAULT_CONTENT_TYPE = "text/plain"
        val DASHES = "--".toByteArray(Charsets.UTF_8)
        val CRLF = "\r\n".toByteArray(Charsets.UTF_8)
    }
}
