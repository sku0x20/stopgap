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
    private val bufferedInputStream = BufferedInputStream(bodyStream)

    fun part(): Part? {
        val boundaryBytes = bufferedInputStream.readNBytes(dashBoundary.size)
        if (!boundaryBytes.contentEquals(dashBoundary)) throw MalformedBodyException(null)
        // "--" closes the body; CRLF means a part follows.
        val nextBytes = bufferedInputStream.readNBytes(2)
        if (nextBytes.contentEquals(DASHES)) return null
        if (!nextBytes.contentEquals(CRLF)) throw MalformedBodyException(null)
        val headers = readHeaders()
        TODO("read content")
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
    // On a mismatch, matching restarts at the current byte. That is enough only because the sequence's first
    // byte (CR) appears nowhere else in it, so a partial match can't hide the start of another.
    private fun readUntil(sequence: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        var matched = 0
        while (matched < sequence.size) {
            val byte = bufferedInputStream.read()
            if (byte < 0) throw MalformedBodyException(null)
            if (byte == sequence[matched].toInt()) {
                matched++
                continue
            }
            output.write(sequence, 0, matched)
            if (byte == sequence[0].toInt()) {
                matched = 1
            } else {
                matched = 0
                output.write(byte)
            }
        }
        return output.toByteArray()
    }

    private companion object {
        val DASHES = "--".toByteArray(Charsets.UTF_8)
        val CRLF = "\r\n".toByteArray(Charsets.UTF_8)
    }
}
