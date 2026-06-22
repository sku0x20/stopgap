package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils
import java.io.InputStream

class RoutesGenerator(
    private val endpointClazz: KSClassDeclaration,
    private val imports: MutableSet<String>,
    private val w: CustomWriter,
) {

    fun write() = w.withRelativeIndent {
        addImports()
        val body = captureBody()
        writeFunctionDefinition()
        withRelativeIndent(4) {
            writeVariables()
            w.write(body)
        }
        writeLine("}")
    }

    fun addImports() {
        imports.add("io.helidon.webserver.http.HttpRouting")
        imports.add("dev.sku20.helidon.serde.CustomSerdeCatalog")
    }

    private val endpoint = "endpoint"
    private val routes = "routes"

    private var params = mutableSetOf(
        "$endpoint: ${endpointClazz.qualifiedName!!.asString()}",
        "$routes: HttpRouting.Builder",
    )

    private fun writeFunctionDefinition() = w.withRelativeIndent {
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            for (param in params) writeLine("$param,")
        }
        writeLine("){")
    }

    private val variables = mutableSetOf<String>()
    private fun writeVariables() = w.withRelativeIndent {
        for (variable in variables) writeLine("val $variable")
    }

    private fun captureBody(): InputStream = Utils.capturing { writer ->
        writer.setIndent(w.indent + 4)
        RoutesBodyGenerator(
            endpointClazz,
            endpoint,
            routes,
            imports,
            params,
            variables,
            writer
        ).write()
    }
}
