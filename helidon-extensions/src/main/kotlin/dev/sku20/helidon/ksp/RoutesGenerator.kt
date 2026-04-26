package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.helidon.endpoint.Endpoint

class RoutesGenerator(
    private val endpointClazz: KSClassDeclaration,
) {
    private val endpointAnnotation = findEndpointAnnotationOnClazz()

    private val endpoint = "endpoint"
    private val routes = "routes"
    private val defaultSerde = "serde"

    private lateinit var w: CustomWriter

    fun write(w: CustomWriter) = w.withRelativeIndent {
        this@RoutesGenerator.w = w
        writeFunctionDefinition()
        withRelativeIndent(4) {
            RoutesBodyGenerator(
                endpointClazz,
                endpointAnnotation,
                endpoint,
                routes,
                defaultSerde,
            ).write(w)
        }
        writeLine("}")
    }

    private fun writeFunctionDefinition() = w.withRelativeIndent {
        val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$endpoint: ${endpointQualifiedName},")
            writeLine("$routes: HttpRouting.Builder,")
            writeLine("$defaultSerde: Serde = NopSerde,")
        }
        writeLine("){")
    }

    private fun findEndpointAnnotationOnClazz(): KSAnnotation =
        endpointClazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
}
