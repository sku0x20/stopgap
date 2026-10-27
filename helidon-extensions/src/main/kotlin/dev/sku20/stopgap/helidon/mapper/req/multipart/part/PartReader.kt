package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
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

    fun part(): Part? {
        val boundaryBytes = bufferedInputStream.readNBytes(dashBoundary.size)
        if (!boundaryBytes.contentEquals(dashBoundary)) throw MalformedBodyException(null)
        // "--" closes the body; CRLF means a part follows.
        val nextBytes = bufferedInputStream.readNBytes(2)
        if (nextBytes.contentEquals(DASHES)) return null
        if (!nextBytes.contentEquals(CRLF)) throw MalformedBodyException(null)
        val headers = readHeaders()
        val content = readUntil(delimiter)
        TODO("build part")
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
        val DASHES = "--".toByteArray(Charsets.UTF_8)
        val CRLF = "\r\n".toByteArray(Charsets.UTF_8)
    }
}
