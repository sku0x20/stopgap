package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.ksp.CustomWriter

class RegistryInitializerGenerator(
    private val functions: List<KSFunctionDeclaration>
) {

    private lateinit var w: CustomWriter
    private lateinit var imports: MutableSet<String>

    fun write(
        w: CustomWriter,
        imports: MutableSet<String>
    ) = w.withRelativeIndent {
        this@RegistryInitializerGenerator.w = w
        this@RegistryInitializerGenerator.imports = imports

        addImports()
        writeFunctionDefinition()
        withRelativeIndent(4) {
            for (function in functions) {
                writeRegisterCallFor(function)
            }
        }
        writeLine("}")
    }

    private fun addImports() {
        imports.add("dev.sku20.ir.InstanceRegistry")
        imports.add("io.helidon.webserver.http.HttpRouting")
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
        val endpointQualifiedName = function.parameters.first().type.resolve().declaration.qualifiedName!!.asString()
        writeLine("registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$registry.getInstanceForType<$endpointQualifiedName>(),")
            writeLine(routes)
        }
        writeLine(")")
    }
}
