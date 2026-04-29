package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.helidon.ksp.CustomWriter

class RegistryInitializerGenerator(
    private val endpointClazzez: List<KSClassDeclaration>
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
            for (endpointClazz in endpointClazzez) {
                writeRegisterCallFor(endpointClazz)
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

    private fun writeRegisterCallFor(endpointClazz: KSClassDeclaration) = w.withRelativeIndent {
        val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
        writeLine("registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$registry.getInstanceForType<$endpointQualifiedName>(),")
            writeLine(routes)
        }
        writeLine(")")
    }
}
