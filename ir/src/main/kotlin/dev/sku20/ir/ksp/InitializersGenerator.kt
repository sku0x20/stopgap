package dev.sku20.ir.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.ir.Creates
import dev.sku20.ir.InstanceRegistry
import java.io.OutputStream

class InitializersGenerator(
    file: OutputStream,
    private val creators: List<KSFunctionDeclaration>,
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
        for (create in creators) {
            val annotation = create.annotations.first { it.shortName.asString() == Creates::class.simpleName }
            val eagerly = annotation.arguments.first { it.name?.getShortName() == "eagerly" }
            val value = eagerly.value as Boolean
            if (value) {
                val returnType = create.returnType!!.resolve()
                val qualifiedName = returnType.declaration.qualifiedName!!.asString()
                writeLine("registry.getInstanceForType<${qualifiedName}>()")
            }
        }
    }

    private fun writeRegistrations() = w.withRelativeIndent {
        for (create in creators) {
            writeLine("registry.registerForType {")
            withRelativeIndent(4) {
                val parameters = StringBuilder()
                for (param in create.parameters) {
                    val paramType = param.type.resolve().declaration
                    val paramQualifiedType = paramType.qualifiedName!!.asString()
                    if (InstanceRegistry::class.qualifiedName!! == paramQualifiedType) parameters.append("registry,")
                    else parameters.append("registry.getInstanceForType<$paramQualifiedType>(),")
                }
                writeLine("${create.qualifiedName!!.asString()}(${parameters})")
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
