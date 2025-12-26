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

    fun withRelativeIndent(n: Int = 0, block: CustomWriter.() -> Unit) {
        increaseIndentBy(n)
        block()
        decreaseIndentBy(n)
    }

    fun increaseIndentBy(n: Int) {
        indent += n
    }

    fun decreaseIndentBy(n: Int) {
        indent -= n
    }

    fun setIndent(n: Int) {
        indent = n
    }

    private var indent = 0

    @Suppress("EmptyRange")
    private fun writeIndent() {
        for (i in 0 until indent) {
            writer.write(" ")
        }
    }


}