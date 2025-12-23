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
        symbols.forEach { symbol ->
            writer.appendLine("registry.registerForType {")
            writer.appendLine("${symbol.qualifiedName!!.asString()}(registry)")
            writer.appendLine("}")
        }
        writeFooter()
        writer.close()
    }

    private fun writeHeader() {
        writer.appendLine("package $packageName")
        writer.appendLine()
        writer.appendLine("import dev.sku20.ir.InstanceRegistry")
        writer.appendLine()
        writer.appendLine("object $fileName {")
        writer.appendLine("fun init(registry: InstanceRegistry) {")
    }

    private fun writeFooter() {
        writer.appendLine("}")
        writer.appendLine("}")
    }

}
