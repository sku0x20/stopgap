package dev.sku20.stopgap.helidon.mapper.req.multipart

import java.io.InputStream

interface PartBody {
    /** Opens a new stream over the whole body on every call. */
    fun inputStream(): InputStream
}
