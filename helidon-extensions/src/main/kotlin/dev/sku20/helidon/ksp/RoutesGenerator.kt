package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.InputStream

class RoutesGenerator(
    private val endpointClazz: KSClassDeclaration,
) {
    private val endpoint = "endpoint"
    private val routes = "routes"
    private val defaultSerde = "serde"

    private lateinit var w: CustomWriter

    fun write(w: CustomWriter) = w.withRelativeIndent {
        this@RoutesGenerator.w = w
        hasRan = true

        val body = captureBody()

        writeFunctionDefinition()
        withRelativeIndent(4) {
            w.write(body)
        }
        writeLine("}")
    }

    private var hasRan = false
    fun imports(): List<String> {
        if (!hasRan) throw IllegalStateException("write() must be called before imports()")
        return listOf(
            "dev.sku20.helidon.serde.Serde",
            "dev.sku20.helidon.serde.NopSerde",
            "io.helidon.webserver.http.HttpRouting",
        )
    }

    private fun captureBody(): InputStream = Utils.capturing { writer ->
        RoutesBodyGenerator(
            endpointClazz,
            endpoint,
            routes,
            defaultSerde,
        ).write(writer)
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
}
