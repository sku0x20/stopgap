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
            "io.helidon.webserver.http.HttpRouting",
            "dev.sku20.helidon.serde.Serde",
            "dev.sku20.helidon.serde.NopSerde",
        )
    }

    private var params = mutableListOf<String>()

    private fun captureBody(): InputStream = Utils.capturing { writer ->
        val gen = RoutesBodyGenerator(endpointClazz, endpoint, routes)
        gen.write(writer)
        params.addAll(gen.params())
    }

    private fun writeFunctionDefinition() = w.withRelativeIndent {
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            for (param in params) writeLine("$param,")
        }
        writeLine("){")
    }
}
