package dev.sku20.ir.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.OutputStream

// todo: rename file to Initializers with fun InitRegistry(registry)
// - registerCreators
// - createInstancesEagerly

class InitCreatorsWriter(
    file: OutputStream,
    private val symbols: List<KSFunctionDeclaration>,
    private val packageName: String,
    private val fileName: String
) {
    private val w = CustomWriter(file)

    fun write() {
        writeHeader()
        registerCreators()
        writeFooter()
        w.close()
    }

    private fun registerCreators() {
        for (symbol in symbols) {
            w.setIndent(8)
            w.writeLine("registry.registerForType {")
            w.setIndent(12)
            w.writeLine("${symbol.qualifiedName!!.asString()}(registry)")
            w.setIndent(8)
            w.writeLine("}")
        }
    }

    private fun writeHeader() {
        w.setIndent(0)
        w.writeLine("package $packageName")
        w.writeLine()
        w.writeLine("import dev.sku20.ir.InstanceRegistry")
        w.writeLine()
        w.writeLine("object $fileName {")
        w.writeLine()
        w.setIndent(4)
        w.writeLine("fun init(registry: InstanceRegistry) {")
    }

    private fun writeFooter() {
        w.setIndent(4)
        w.writeLine("}")
        w.writeLine()
        w.setIndent(0)
        w.writeLine("}")
    }

}
