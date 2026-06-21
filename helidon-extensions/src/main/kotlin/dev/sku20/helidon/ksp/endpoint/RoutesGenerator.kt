package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils
import java.io.InputStream

class RoutesGenerator(
    private val endpointClazz: KSClassDeclaration,
    private val w: CustomWriter,
    private val imports: MutableSet<String>,
) {

    fun write() = w.withRelativeIndent {
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
        imports.add("dev.sku20.helidon.ksp.RegistryQualifier")
    }

    private val endpoint = "endpoint"
    private val routes = "routes"

    private var params = mutableSetOf(
        "$endpoint: ${endpointClazz.qualifiedName!!.asString()}",
        "$routes: HttpRouting.Builder",
    )

    private fun captureBody(): InputStream = Utils.capturing { writer ->
        RoutesBodyGenerator(endpointClazz, endpoint, routes, writer, imports, params).write()
    }

    private fun writeFunctionDefinition() = w.withRelativeIndent {
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            for (param in params) writeLine("$param,")
        }
        writeLine("){")
    }
}
