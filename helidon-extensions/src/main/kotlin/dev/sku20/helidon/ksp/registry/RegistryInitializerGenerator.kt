package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.endpoint.EndpointSymbolProcessor
import dev.sku20.helidon.serde.CustomSerdeCatalog
import java.io.OutputStream
import kotlin.reflect.KClass

class RegistryInitializerGenerator(
    file: OutputStream,
    private val functions: List<KSFunctionDeclaration>,
    private val packageName: String
) {
    private val w = CustomWriter(file)

    fun write() {
        writePackage()
        writeImports()
        writeFunctionDefinition()
        w.withRelativeIndent(4) {
            for (function in functions) writeRegisterCallFor(function)
        }
        w.writeLine("}")
        w.close()
    }

    private val registry = "registry"
    private val routes = "routes"

    private fun writeFunctionDefinition() = w.withRelativeIndent {
        writeLine("fun initEndpointsRoutesViaRegistry(")
        withRelativeIndent(4) {
            writeLine("$registry: InstanceRegistry,")
            writeLine("$routes: HttpRouting.Builder,")
        }
        writeLine(") {")
    }

    private fun writeRegisterCallFor(function: KSFunctionDeclaration) = w.withRelativeIndent {
        writeLine("registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$registry.getInstanceForType<${getParamFQN(function.parameters.first())}>(),")
            writeLine("$routes,")
            writeExtraParams(function)
        }
        writeLine(")")
    }

    private fun writeExtraParams(function: KSFunctionDeclaration) = w.withRelativeIndent {
        for (i in 2 until function.parameters.size) {
            val param = function.parameters[i]
            val annotation = getAnnotation(param, CustomSerdeCatalog::class)
            val qualifier = fieldValue(annotation, "qualifier") as String
            if (qualifier.isNotEmpty()) {
                writeLine("$registry.getInstanceForQualifier(\"$qualifier\"),")
            } else {
                val type = fieldValue(annotation, "type") as KClass<*>
                writeLine("$registry.getInstanceForType<${type.qualifiedName}>(),")
            }
        }
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        writeLine("import dev.sku20.ir.InstanceRegistry")
        writeLine("import io.helidon.webserver.http.HttpRouting")
        writeLine("import ${EndpointSymbolProcessor.GENERATED_PACKAGE}.registerRoutesFor")
        writeLine()
    }

    private fun getAnnotation(param: KSValueParameter, klass: KClass<*>): KSAnnotation =
        param.annotations.first { it.shortName.asString() == klass.simpleName }

    private fun fieldValue(annotation: KSAnnotation, fieldName: String): Any {
        val fieldValue = annotation.arguments.first { it.name?.getShortName() == fieldName }
        return fieldValue.value as Any
    }

    private fun getParamFQN(param: KSValueParameter): String {
        return param.type.resolve().declaration.qualifiedName!!.asString()
    }
}
