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
    // The window slides one byte at a time, so sequence is found wherever it starts.
    private fun readUntil(sequence: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        val window = bufferedInputStream.readNBytes(sequence.size)
        if (window.size < sequence.size) throw MalformedBodyException(null)
        while (!window.contentEquals(sequence)) {
            output.write(window[0].toInt())
            System.arraycopy(window, 1, window, 0, window.size - 1)
            val byte = bufferedInputStream.read()
            if (byte < 0) throw MalformedBodyException(null)
            window[window.size - 1] = byte.toByte()
        }
        return output.toByteArray()
    }

    private companion object {
        val DASHES = "--".toByteArray(Charsets.UTF_8)
        val CRLF = "\r\n".toByteArray(Charsets.UTF_8)
    }
}
