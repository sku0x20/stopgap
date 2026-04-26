package dev.sku20.helidon.ksp

import java.io.InputStream
import java.io.OutputStream

/**
 * A write-then-read buffer with zero copy and progressive memory release.
 *
 * Java's InputStream/OutputStream are fundamentally split abstractions — there is no
 * built-in JVM type that lets you write to a buffer and read back without copying.
 * ByteArrayOutputStream.toByteArray() allocates a full copy: 2x memory, wasted CPU.
 * For large outputs (e.g. multi-GB generated files) this is completely unacceptable.
 * High-performance frameworks like Netty solved this by writing their own buffer type
 * (ByteBuf). We do the same here.
 *
 * Usage: write to [outputStream], close it to seal, then read from [inputStream].
 * Memory is released chunk by chunk as data is consumed.
 */
class CopyFreeBuffer(private val chunkSize: Int = 8 * 1024) {

    private val chunks = ArrayDeque<ByteArray>()
    private var writeChunk = ByteArray(chunkSize)
    private var writePos = 0

    val outputStream: OutputStream = object : OutputStream() {

        override fun write(b: Int) {
            ensureSpace()
            writeChunk[writePos++] = b.toByte()
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            var remaining = len
            var srcPos = off
            while (remaining > 0) {
                ensureSpace()
                val toWrite = minOf(writeChunk.size - writePos, remaining)
                System.arraycopy(b, srcPos, writeChunk, writePos, toWrite)
                writePos += toWrite
                srcPos += toWrite
                remaining -= toWrite
            }
        }

        private fun ensureSpace() {
            if (writePos == writeChunk.size) {
                chunks.addLast(writeChunk)
                writeChunk = ByteArray(chunkSize)
                writePos = 0
            }
        }

        override fun close() {
            if (writePos > 0) {
                chunks.addLast(writeChunk.copyOf(writePos))
                writePos = 0
            }
        }
    }

    val inputStream: InputStream = object : InputStream() {

        private var readChunk: ByteArray? = null
        private var readPos = 0

        override fun read(): Int {
            val b = ByteArray(1)
            return if (read(b, 0, 1) == -1) -1 else b[0].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            var chunk = readChunk
            if (chunk == null || readPos == chunk.size) {
                if (chunks.isEmpty()) return -1
                chunk = chunks.removeFirst()
                readChunk = chunk
                readPos = 0
            }
            val toRead = minOf(len, chunk.size - readPos)
            System.arraycopy(chunk, readPos, b, off, toRead)
            readPos += toRead
            if (readPos == chunk.size) readChunk = null  // release for GC
            return toRead
        }
    }
}
