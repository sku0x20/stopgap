package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import io.helidon.http.HttpMediaType
import java.io.InputStream
import java.nio.file.Path

// Reads the body through a fixed buffer, so memory stays flat however large the parts are.
// Every delimiter after the first is preceded by CRLF, which belongs to the delimiter, not to the part's content.
internal class MultiPartParser(
    private val input: InputStream,
    boundary: String,
    private val memoryThreshold: Int,
    private val tempDirectory: Path,
) {

    private val delimiter = "\r\n--$boundary".toByteArray(Charsets.US_ASCII)
    private val buffer = ByteArray(BUFFER_SIZE)
    private var start = 0
    private var end = 0
    private var bytesRead = 0L

    init {
        if (boundary.length > MAX_BOUNDARY_LENGTH) throw MalformedBodyException(null)
        // A virtual CRLF lets the first delimiter match even when the body opens with it.
        buffer[0] = CR
        buffer[1] = LF
        end = 2
    }

    // On failure the endpoint never runs and nothing else would close the bodies, so delete them here.
    fun parse(): MultiPart {
        val bodies = mutableListOf<PartBody>()
        var spool: PartBodySpool? = null
        try {
            var isClosed = readToDelimiter(DISCARD)
            val parts = mutableListOf<Part>()
            while (!isClosed) {
                val headers = readHeaders()
                val current = PartBodySpool(memoryThreshold, tempDirectory)
                spool = current
                isClosed = readToDelimiter(current::write)
                val body = current.toBody()
                spool = null
                bodies.add(body)
                parts.add(toPart(headers, body))
            }
            return MultiPart(parts)
        } catch (e: Exception) {
            if (spool != null) spool.discard()
            for (body in bodies) {
                body.close()
            }
            throw e
        }
    }

    // Copies bytes up to the next delimiter into sink and consumes the delimiter; true if it closes the body.
    private fun readToDelimiter(sink: (ByteArray, Int, Int) -> Unit): Boolean {
        while (true) {
            val index = indexOfDelimiter()
            if (index < 0) {
                // A delimiter may start in the last bytes, so keep them for the next read.
                val safeEnd = end - delimiter.size + 1
                if (safeEnd > start) {
                    sink(buffer, start, safeEnd - start)
                    start = safeEnd
                }
                if (!read()) endOfInput()
                continue
            }
            sink(buffer, start, index - start)
            start = index
            if (peek(delimiter.size) == DASH && peek(delimiter.size + 1) == DASH) {
                start += delimiter.size + 2
                return true
            }
            val lineEnd = paddedLineEndLength(delimiter.size)
            if (lineEnd < 0) {
                // Only a prefix of a longer line, e.g. --abcd for boundary abc: it is content.
                sink(buffer, start, 1)
                start += 1
                continue
            }
            start += delimiter.size + lineEnd
            return false
        }
    }

    // RFC 2046 allows spaces and tabs between the boundary and its CRLF.
    private fun paddedLineEndLength(offset: Int): Int {
        var i = offset
        while (peek(i) == SPACE || peek(i) == TAB) i++
        if (peek(i) == CR.toInt() && peek(i + 1) == LF.toInt()) return i + 2 - offset
        return -1
    }

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
        while (true) {
            for (i in start until end - 1) {
                if (buffer[i] == CR && buffer[i + 1] == LF) {
                    val line = String(buffer, start, i - start, Charsets.UTF_8)
                    start = i + 2
                    return line
                }
            }
            if (!read()) throw MalformedBodyException(null)
        }
    }

    private fun toPart(headers: Map<String, String>, body: PartBody): Part {
        val disposition = headers["content-disposition"] ?: throw MalformedBodyException(null)
        val parameters = ContentDispositionParser(disposition).parse()
        val name = parameters["name"] ?: throw MalformedBodyException(null)
        return Part(name, parameters["filename"], contentType(headers["content-type"]), body)
    }

    private fun contentType(value: String?): HttpMediaType {
        if (value == null) return DEFAULT_CONTENT_TYPE
        try {
            return HttpMediaType.create(value)
        } catch (e: IllegalArgumentException) {
            throw MalformedBodyException(e)
        }
    }

    private fun indexOfDelimiter(): Int {
        for (i in start..end - delimiter.size) {
            if (isDelimiterAt(i)) return i
        }
        return -1
    }

    private fun isDelimiterAt(index: Int): Boolean {
        for (i in delimiter.indices) {
            if (buffer[index + i] != delimiter[i]) return false
        }
        return true
    }

    // Byte at start + offset, reading more as needed; -1 at end of input or past the buffer.
    private fun peek(offset: Int): Int {
        while (start + offset >= end) {
            if (offset >= buffer.size || !read()) return -1
        }
        return buffer[start + offset].toInt()
    }

    // Moves unread bytes to the front and reads more; false at end of input or when the buffer is full.
    private fun read(): Boolean {
        if (start > 0) {
            System.arraycopy(buffer, start, buffer, 0, end - start)
            end -= start
            start = 0
        }
        if (end == buffer.size) return false
        val count = input.read(buffer, end, buffer.size - end)
        if (count < 0) return false
        end += count
        bytesRead += count
        return true
    }

    private fun endOfInput(): Nothing {
        if (bytesRead == 0L) throw EmptyBodyException()
        throw MalformedBodyException(null)
    }

    private companion object {
        const val BUFFER_SIZE = 8192
        const val MAX_BOUNDARY_LENGTH = 70
        const val CR = '\r'.code.toByte()
        const val LF = '\n'.code.toByte()
        const val DASH = '-'.code
        const val SPACE = ' '.code
        const val TAB = '\t'.code
        val DEFAULT_CONTENT_TYPE: HttpMediaType = HttpMediaType.create("text/plain")
        val DISCARD: (ByteArray, Int, Int) -> Unit = { _, _, _ -> }
    }
}
