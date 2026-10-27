package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.FilePartContent
import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.InMemoryPartContent
import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.PartContent
import java.io.ByteArrayOutputStream

// Keeps content in memory up to bufferSize bytes; past that, the buffer is moved to a file each time it fills.
class PartContentWriter(
    private val bufferSize: Int
) {

    private val buffer = ByteArrayOutputStream()
    private var file: FilePartContent? = null

    fun write(byte: Int) {
        if (buffer.size() == bufferSize) moveBufferToFile()
        buffer.write(byte)
    }

    fun content(): PartContent {
        val current = file ?: return InMemoryPartContent(buffer.toByteArray())
        current.write(buffer.toByteArray())
        current.finishWriting()
        return current
    }

    private fun moveBufferToFile() {
        val current = file ?: startFile()
        current.write(buffer.toByteArray())
        buffer.reset()
    }

    private fun startFile(): FilePartContent {
        val newFile = FilePartContent()
        newFile.startWriting()
        file = newFile
        return newFile
    }
}
