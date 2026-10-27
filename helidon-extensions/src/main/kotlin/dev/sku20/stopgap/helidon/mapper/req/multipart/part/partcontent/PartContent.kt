package dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent

import java.io.InputStream

interface PartContent {
    val size: Long
    fun inputStream(): InputStream
}