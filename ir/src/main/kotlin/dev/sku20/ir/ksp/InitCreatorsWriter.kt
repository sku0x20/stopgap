package dev.sku20.ir.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.OutputStream

class InitCreatorsWriter(
    file: OutputStream,
    private val symbols: List<KSFunctionDeclaration>,
    private val packageName: String,
    private val fileName: String
) {
    private val writer = file.bufferedWriter()

    fun write() {
        writeHeader()
        for (symbol in symbols) {
            setIndent(8)
            writeLine("registry.registerForType {")
            setIndent(12)
            writeLine("${symbol.qualifiedName!!.asString()}(registry)")
            setIndent(8)
            writeLine("}")
        }
        writeFooter()
        writer.close()
    }

    private fun writeHeader() {
        setIndent(0)
        writeLine("package $packageName")
        writeLine()
        writeLine("import dev.sku20.ir.InstanceRegistry")
        writeLine()
        writeLine("object $fileName {")
        writeLine()
        setIndent(4)
        writeLine("fun init(registry: InstanceRegistry) {")
    }

    private fun writeFooter() {
        setIndent(4)
        writeLine("}")
        writeLine()
        setIndent(0)
        writeLine("}")
    }

    private var indent = 0
    private fun setIndent(n: Int) {
        indent = n
    }

    private fun writeLine(s: String = "") {
        writeIndent()
        writer.write("")
        writer.write(s)
        writer.write("\n")
    }

    @Suppress("EmptyRange")
    private fun writeIndent() {
        for (i in 0 until indent) {
            writer.write(" ")
        }
    }

}
