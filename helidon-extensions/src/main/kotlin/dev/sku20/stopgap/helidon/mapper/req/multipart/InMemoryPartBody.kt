package dev.sku20.stopgap.helidon.mapper.req.multipart

import java.io.ByteArrayInputStream
import java.io.InputStream

// Compares by content, so a Part holding it compares by value.
class InMemoryPartBody(private val bytes: ByteArray) : PartBody {

    override fun inputStream(): InputStream = ByteArrayInputStream(bytes)

    override fun equals(other: Any?): Boolean = other is InMemoryPartBody && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String = "InMemoryPartBody(${String(bytes, Charsets.UTF_8)})"
}
