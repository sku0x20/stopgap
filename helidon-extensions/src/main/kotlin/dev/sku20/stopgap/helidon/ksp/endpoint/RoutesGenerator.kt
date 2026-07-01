package dev.sku20.stopgap.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.stopgap.helidon.ksp.CustomWriter
import dev.sku20.stopgap.helidon.ksp.Utils
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
        imports.add("dev.sku20.stopgap.helidon.serde.CustomSerdeCatalog")
    }

    private var params = mutableSetOf(
        "${GeneratedNames.ENDPOINT}: ${endpointClazz.qualifiedName!!.asString()}",
        "${GeneratedNames.ROUTES}: HttpRouting.Builder",
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
            imports,
            params,
            variables,
            writer
        ).write()
    }
}
