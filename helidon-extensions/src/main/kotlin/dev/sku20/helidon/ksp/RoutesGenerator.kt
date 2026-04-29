package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.InputStream

class RoutesGenerator(
    private val endpointClazz: KSClassDeclaration,
) {

    private lateinit var w: CustomWriter
    private lateinit var imports: MutableSet<String>

    fun write(
        w: CustomWriter,
        imports: MutableSet<String>
    ) = w.withRelativeIndent {
        this@RoutesGenerator.w = w
        this@RoutesGenerator.imports = imports

        addImports()
        val body = captureBody()
        writeFunctionDefinition()
        withRelativeIndent(4) {
            w.write(body)
        }
        writeLine("}")
    }

    fun addImports() {
        imports.add("io.helidon.webserver.http.HttpRouting")
        imports.add("dev.sku20.helidon.serde.Serde")
    }

    private val endpoint = "endpoint"
    private val routes = "routes"

    private var params = mutableListOf(
        "$endpoint: ${endpointClazz.qualifiedName!!.asString()}",
        "$routes: HttpRouting.Builder",
    )

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
