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

        val bg = RoutesBodyGenerator(endpointClazz, endpoint, routes, defaultSerde)
        val body = captureBody(bg)

        writeFunctionDefinition(bg)
        withRelativeIndent(4) {
            w.write(body)
        }
        writeLine("}")
    }

    private var hasRan = false
    fun imports(): List<String> {
        if (!hasRan) throw IllegalStateException("write() must be called before imports()")
        return listOf(
            "io.helidon.webserver.http.HttpRouting",
            "dev.sku20.helidon.serde.Serde",
            "dev.sku20.helidon.serde.NopSerde",
        )
    }

    private fun captureBody(bg: RoutesBodyGenerator): InputStream = Utils.capturing { writer ->
        bg.write(writer)
    }

    private fun writeFunctionDefinition(bg: RoutesBodyGenerator) = w.withRelativeIndent {
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            for (param in bg.params()) writeLine("$param,")
        }
        writeLine("){")
    }
}
