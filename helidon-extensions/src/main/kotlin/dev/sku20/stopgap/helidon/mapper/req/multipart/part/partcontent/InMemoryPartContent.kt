package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import java.io.ByteArrayInputStream
import java.io.InputStream

class InMemoryPartContent(
    private val bytes: ByteArray
) : PartContent {

    init {
        require(bytes.size <= MAX_SIZE) { "In-memory part content exceeds $MAX_SIZE bytes: ${bytes.size}" }
    }

    override val size: Long = bytes.size.toLong()

    override fun inputStream(): InputStream = ByteArrayInputStream(bytes)

    companion object {
        const val MAX_SIZE = 16 * 1024
    }
}
