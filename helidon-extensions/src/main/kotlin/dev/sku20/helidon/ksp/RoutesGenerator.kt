package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.ByteArrayOutputStream

class RoutesGenerator(
    private val endpointClazz: KSClassDeclaration,
) {
    private val endpoint = "endpoint"
    private val routes = "routes"
    private val defaultSerde = "serde"

    private lateinit var w: CustomWriter

    fun write(w: CustomWriter) = w.withRelativeIndent {
        this@RoutesGenerator.w = w
        writeFunctionDefinition()
        withRelativeIndent(4) {
            w.write(captureBody())
        }
        writeLine("}")
    }

    private fun captureBody(): ByteArrayOutputStream {
        val output = ByteArrayOutputStream()
        val bodyWriter = CustomWriter(output)
        RoutesBodyGenerator(
            endpointClazz,
            endpoint,
            routes,
            defaultSerde,
        ).write(bodyWriter)
        bodyWriter.close()
        return output
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
