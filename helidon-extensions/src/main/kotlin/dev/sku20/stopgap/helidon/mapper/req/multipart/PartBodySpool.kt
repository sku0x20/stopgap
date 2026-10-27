package dev.sku20.stopgap.helidon.mapper.req.multipart

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

// Holds a part in memory up to memoryThreshold bytes, then moves it to a temp file and keeps writing there.
internal class PartBodySpool(
    private val memoryThreshold: Int,
    private val tempDirectory: Path,
) {

    private val memory = ByteArrayOutputStream()
    private lateinit var file: Path
    private var fileStream: OutputStream? = null

    fun write(bytes: ByteArray, offset: Int, length: Int) {
        val stream = fileStream
        if (stream != null) {
            stream.write(bytes, offset, length)
            return
        }
        if (memory.size() + length <= memoryThreshold) {
            memory.write(bytes, offset, length)
            return
        }
        file = Files.createTempFile(tempDirectory, "multipart-", ".part")
        val newStream = BufferedOutputStream(Files.newOutputStream(file))
        fileStream = newStream
        memory.writeTo(newStream)
        newStream.write(bytes, offset, length)
    }

    fun toBody(): PartBody {
        val stream = fileStream ?: return InMemoryPartBody(memory.toByteArray())
        stream.close()
        return TempFilePartBody(file)
    }

    fun discard() {
        val stream = fileStream ?: return
        try {
            stream.close()
        } catch (e: IOException) {
            // the file is deleted next either way
        }
        TempFilePartBody(file).close()
    }
}
