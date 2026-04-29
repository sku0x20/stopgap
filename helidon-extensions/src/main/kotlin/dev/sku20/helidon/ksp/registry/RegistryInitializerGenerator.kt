package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.endpoint.EndpointSymbolProcessor
import java.io.OutputStream

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
            writeLine(routes)
        }
        writeLine(")")
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        writeLine("import dev.sku20.ir.InstanceRegistry")
        writeLine("import io.helidon.webserver.http.HttpRouting")
        writeLine("import ${EndpointSymbolProcessor.GENERATED_PACKAGE}")
        writeLine()
    }

    private fun getParamFQN(param: KSValueParameter): String {
        return param.type.resolve().declaration.qualifiedName!!.asString()
    }
}
