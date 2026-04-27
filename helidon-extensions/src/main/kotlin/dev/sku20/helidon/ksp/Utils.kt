package dev.sku20.helidon.ksp

import java.io.ByteArrayOutputStream
import java.io.InputStream

object Utils {
    fun capturing(block: (CustomWriter) -> Unit): InputStream {
        val buffer = ByteArrayOutputStream()
        val writer = CustomWriter(buffer)
        block(writer)
        writer.close()
        return buffer.toByteArray().inputStream()
    }
}
