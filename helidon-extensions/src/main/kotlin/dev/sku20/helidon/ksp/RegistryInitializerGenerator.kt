package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration

class RegistryInitializerGenerator(
    private val endpointClazzez: List<KSClassDeclaration>
) {
    private lateinit var w: CustomWriter

    fun write(w: CustomWriter) = w.withRelativeIndent {
        this@RegistryInitializerGenerator.w = w
        writeLine("fun initEndpointsRoutesViaRegistry(registry: InstanceRegistry, routes: HttpRouting.Builder) {")
        withRelativeIndent(4) {
            for (endpointClazz in endpointClazzez) {
                val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
                writeLine("registerRoutesFor(")
                withRelativeIndent(4) {
                    writeLine("registry.getInstanceForType<$endpointQualifiedName>(),")
                    writeLine("routes")
                }
                writeLine(")")
            }
        }
        writeLine("}")
    }
}
