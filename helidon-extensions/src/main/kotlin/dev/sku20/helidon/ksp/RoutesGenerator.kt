package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration

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
            w.write(captureBody().inputStream)
        }
        writeLine("}")
    }

    private fun captureBody(): CopyFreeBuffer {
        val buffer = CopyFreeBuffer()
        val bodyWriter = CustomWriter(buffer.outputStream)
        RoutesBodyGenerator(
            endpointClazz,
            endpoint,
            routes,
            defaultSerde,
        ).write(bodyWriter)
        bodyWriter.close()
        return buffer
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
