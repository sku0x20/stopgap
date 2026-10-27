package dev.sku20.stopgap.helidon.mapper.req.multipart

import java.io.InputStream

/** [close] releases whatever holds the body; it never throws and is safe to call twice. */
interface PartBody : AutoCloseable {
    /** Bytes in the body, known without reading it, e.g. to reject a file that is too large. */
    val size: Long

    /** Opens a new stream over the whole body on every call. */
    fun inputStream(): InputStream

    override fun close()
}
