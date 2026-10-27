package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.multipart.Part
import java.io.InputStream

class PartParser(
    private val bodyStream: InputStream
) {

    fun part(): Part {
        TODO()
    }
}
