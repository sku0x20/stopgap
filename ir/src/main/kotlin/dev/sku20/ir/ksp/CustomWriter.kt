package dev.sku20.ir.ksp

import java.io.OutputStream

class CustomWriter(
    output: OutputStream
) {
    private val writer = output.bufferedWriter()

    fun close() {
        writer.close()
    }

    fun writeLine(s: String = "") {
        writeIndent()
        writer.write(s)
        writer.write("\n")
    }

    private var indent = 0

    fun setIndent(n: Int) {
        indent = n
    }

    @Suppress("EmptyRange")
    private fun writeIndent() {
        for (i in 0 until indent) {
            writer.write(" ")
        }
    }


}