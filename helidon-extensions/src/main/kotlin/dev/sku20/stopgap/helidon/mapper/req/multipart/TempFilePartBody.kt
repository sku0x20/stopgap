package dev.sku20.stopgap.helidon.mapper.req.multipart

import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class TempFilePartBody internal constructor(
    private val path: Path,
    override val size: Long,
) : PartBody {

    override fun inputStream(): InputStream = Files.newInputStream(path)

    // Logs instead of throwing: close runs in a finally block, where an exception would hide the endpoint's own.
    override fun close() {
        try {
            Files.deleteIfExists(path)
        } catch (e: IOException) {
            LOGGER.log(System.Logger.Level.WARNING, "Could not delete multipart temp file $path", e)
        }
    }

    override fun toString(): String = "TempFilePartBody($path)"

    private companion object {
        val LOGGER: System.Logger = System.getLogger(TempFilePartBody::class.java.name)
    }
}
