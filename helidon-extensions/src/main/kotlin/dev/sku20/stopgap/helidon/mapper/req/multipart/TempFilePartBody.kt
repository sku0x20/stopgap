package dev.sku20.stopgap.helidon.mapper.req.multipart

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class TempFilePartBody internal constructor(private val path: Path) : PartBody {

    override fun inputStream(): InputStream = Files.newInputStream(path)

    override fun toString(): String = "TempFilePartBody($path)"
}
