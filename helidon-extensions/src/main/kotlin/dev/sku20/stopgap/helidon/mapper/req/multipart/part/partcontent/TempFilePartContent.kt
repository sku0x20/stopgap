package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class TempFilePartContent(
    private val path: Path = Files.createTempFile("stopgap-multipart-", ".bin")
) : PartContent {

    override val size: Long = Files.size(path)

    override fun inputStream(): InputStream = Files.newInputStream(path)
}
