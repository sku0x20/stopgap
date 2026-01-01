package dev.sku20.ir.ksp

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.ir.Creates
import dev.sku20.ir.InstanceRegistry
import dev.sku20.ir.Qualifier
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

    private fun writeRegistrations() {
        for (create in creators) {
            val qualifierAnnotation = qualifierAnnotation(create.annotations)
            if (qualifierAnnotation != null) registerViaQualifier(create, qualifierAnnotation)
            else registerViaType(create)
        }
    }

    private fun registerViaType(create: KSFunctionDeclaration) = w.withRelativeIndent {
        writeLine("registry.registerForType {")
        withRelativeIndent(4) {
            callCreateFun(create)
        }
        writeLine("}")
    }

    private fun registerViaQualifier(create: KSFunctionDeclaration, qualifier: KSAnnotation) = w.withRelativeIndent {
        val qualifierValue = getQualifierValue(qualifier)
        writeLine("registry.registerForQualifier(\"$qualifierValue\") {")
        withRelativeIndent(4) {
            callCreateFun(create)
        }
        writeLine("}")
    }

    private fun callCreateFun(create: KSFunctionDeclaration) = w.withRelativeIndent {
        writeLine("${create.qualifiedName!!.asString()}(")
        withRelativeIndent(4) {
            for (param in create.parameters) {
                val qualifierAnnotation = qualifierAnnotation(param.annotations)
                if (qualifierAnnotation != null) getInstanceViaQualifier(qualifierAnnotation)
                else getInstanceViaType(param)
            }
        }
        writeLine(")")
    }

    private fun getInstanceViaQualifier(qualifier: KSAnnotation) = w.withRelativeIndent {
        val qualifierValue = getQualifierValue(qualifier)
        writeLine("registry.getInstanceForQualifier(\"$qualifierValue\"),")
    }

    private fun getInstanceViaType(param: KSValueParameter) = w.withRelativeIndent {
        val paramType = param.type.resolve().declaration
        val paramQualifiedType = paramType.qualifiedName!!.asString()
        if (InstanceRegistry::class.qualifiedName!! == paramQualifiedType) writeLine("registry,")
        else writeLine("registry.getInstanceForType<$paramQualifiedType>(),")
    }

    private fun qualifierAnnotation(annotations: Sequence<KSAnnotation>): KSAnnotation? =
        annotations.firstOrNull { it.shortName.asString() == Qualifier::class.simpleName }

    private fun getQualifierValue(qualifier: KSAnnotation): String =
        qualifier.arguments.first { it.name?.getShortName() == "value" }.value as String

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        writeLine("import dev.sku20.ir.InstanceRegistry")
        writeLine()
    }

}
