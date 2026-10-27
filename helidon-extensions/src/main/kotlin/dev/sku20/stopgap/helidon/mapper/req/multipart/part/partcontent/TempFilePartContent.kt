package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import java.io.BufferedOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class TempFilePartContent(
    private val path: Path = Files.createTempFile("stopgap-multipart-", ".bin")
) : PartContent {

    private var outputStream: OutputStream? = null

    fun startWriting() {
        val stream = BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.APPEND))
        outputStream = stream
    }

    // close() flushes the buffer first.
    fun finishWriting() {
        val stream = checkNotNull(outputStream) { "startWriting() first" }
        stream.close()
        outputStream = null
    }

    fun write(data: ByteArray) {
        val stream = checkNotNull(outputStream) { "startWriting() first" }
        stream.write(data)
    }

    // Reads need finishWriting() first, or they would miss bytes still in the buffer.
    override val size: Long by lazy {
        check(outputStream == null) { "finishWriting() before reading" }
        Files.size(path)
    }

    override fun inputStream(): InputStream {
        check(outputStream == null) { "finishWriting() before reading" }
        return Files.newInputStream(path)
    }

    // Never throws: close runs in a finally, where an exception would hide the endpoint's own.
    override fun close() {
        try {
            Files.deleteIfExists(path)
        } catch (e: IOException) {
            // TODO: log the failed delete.
        }
    }
}
