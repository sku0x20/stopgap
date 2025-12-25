package dev.sku20.ir.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.OutputStream

class InitializersGenerator(
    file: OutputStream,
    private val symbols: List<KSFunctionDeclaration>,
    private val packageName: String
) {
    private val w = CustomWriter(file)

    fun write() {
        writePackage()
        writeImports()
        writeFunInitRegistry()
        writePrivateFunRegisterCreators()
        w.close()
    }

    private fun writeFunInitRegistry() = w.withRelativeIndent {
        writeLine("fun initRegistry(registry: InstanceRegistry) {")
        withRelativeIndent(4) {
            writeLine("registerCreators(registry)")
            writeLine("// createInstancesEagerly(registry)")
        }
        writeLine("}")
    }

    private fun writePrivateFunRegisterCreators() = w.withRelativeIndent {
        writeLine("private fun registerCreators(registry: InstanceRegistry) {")
        withRelativeIndent(4) {
            writeRegistrations()
        }
        writeLine("}")
    }

    private fun writeRegistrations() = w.withRelativeIndent {
        for (symbol in symbols) {
            writeLine("registry.registerForType {")
            withRelativeIndent(4) {
                writeLine("${symbol.qualifiedName!!.asString()}(registry)")
            }
            writeLine("}")
        }
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        writeLine("import dev.sku20.ir.InstanceRegistry")
        writeLine()
    }

}
