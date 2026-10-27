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

    // Opened on the first write, so constructing over an existing file never touches it.
    private var outputStream: OutputStream? = null

    // Reads flush first, so they see every byte written so far.
    override val size: Long
        get() {
            flush()
            return Files.size(path)
        }

    override fun inputStream(): InputStream {
        flush()
        return Files.newInputStream(path)
    }

    // Never throws: close runs in a finally, where an exception would hide the endpoint's own.
    override fun close() {
        try {
            val stream = outputStream
            if (stream != null) stream.close()
        } catch (e: IOException) {
            // the file is deleted next either way
        }
        try {
            Files.deleteIfExists(path)
        } catch (e: IOException) {
            // TODO: log the failed delete.
        }
    }

    fun write(data: ByteArray) {
        val stream = outputStream ?: openOutputStream()
        stream.write(data)
    }

    private fun openOutputStream(): OutputStream {
        val stream = BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.APPEND))
        outputStream = stream
        return stream
    }

    private fun flush() {
        val stream = outputStream ?: return
        stream.flush()
    }
}
