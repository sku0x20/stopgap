package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import java.io.InputStream

/** [close] releases the storage behind the content, e.g. deletes its temp file. */
interface PartContent : AutoCloseable {
    val size: Long
    fun inputStream(): InputStream
    override fun close()
}