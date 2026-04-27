package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration

class RegistryInitializerGenerator(
    private val endpointClazzez: List<KSClassDeclaration>
) {

    private lateinit var w: CustomWriter

    fun imports(): List<String> = listOf(
        "dev.sku20.ir.InstanceRegistry",
        "io.helidon.webserver.http.HttpRouting",
    )

    fun write(w: CustomWriter) = w.withRelativeIndent {
        this@RegistryInitializerGenerator.w = w
        writeFunctionDefinition()
        withRelativeIndent(4) {
            for (endpointClazz in endpointClazzez) {
                writeRegisterCallFor(endpointClazz)
            }
        }
        writeLine("}")
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
