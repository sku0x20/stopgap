package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class TempFilePartContent(
    private val path: Path = Files.createTempFile("stopgap-multipart-", ".bin")
) : PartContent {

    override val size: Long
        get() = Files.size(path)

    override fun inputStream(): InputStream = Files.newInputStream(path)

    // Never throws: close runs in a finally, where an exception would hide the endpoint's own.
    override fun close() {
        try {
            Files.deleteIfExists(path)
        } catch (e: IOException) {
            // TODO: log the failed delete.
        }
    }

    fun write(data: ByteArray) {
        TODO()
    }
}
