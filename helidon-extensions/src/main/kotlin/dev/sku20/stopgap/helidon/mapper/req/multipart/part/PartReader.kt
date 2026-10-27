package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import java.io.InputStream

class PartReader(
    private val boundary: String,
    private val bodyStream: InputStream
) {

    fun part(): Part? {
        TODO()
    }
}
