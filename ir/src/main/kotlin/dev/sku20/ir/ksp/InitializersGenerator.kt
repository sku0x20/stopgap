package dev.sku20.ir.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.ir.Creates
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
        writePrivateFunCreateInstancesEagerly()
        w.close()
    }

    private fun writeFunInitRegistry() = w.withRelativeIndent {
        writeLine("fun initRegistry(registry: InstanceRegistry) {")
        withRelativeIndent(4) {
            writeLine("registerCreators(registry)")
            writeLine("createInstancesEagerly(registry)")
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

    private fun writePrivateFunCreateInstancesEagerly() = w.withRelativeIndent {
        writeLine("private fun createInstancesEagerly(registry: InstanceRegistry) {")
        withRelativeIndent(4) {
            writeCreateInstancesEagerly()
        }
        writeLine("}")
    }

    private fun writeCreateInstancesEagerly() = w.withRelativeIndent {
        for (symbol in symbols) {
            val annotation = symbol.annotations.first { it.shortName.asString() == Creates::class.simpleName }
            val eagerly = annotation.arguments.first { it.name?.getShortName() == "eagerly" }
            val value = eagerly.value as Boolean
            if (value) {
                val returnType = symbol.returnType!!.resolve()
                val qualifiedName = returnType.declaration.qualifiedName!!.asString()
                writeLine("registry.getInstanceForType<${qualifiedName}>()")
            }
        }
    }

    private fun writeRegistrations() = w.withRelativeIndent {
        for (symbol in symbols) {
            writeLine("registry.registerForType {")
            withRelativeIndent(4) {
                writeLine("${symbol.qualifiedName!!.asString()}(")
                withRelativeIndent(4) {
                    writeLine("registry,")
                }
                writeLine(")")
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
