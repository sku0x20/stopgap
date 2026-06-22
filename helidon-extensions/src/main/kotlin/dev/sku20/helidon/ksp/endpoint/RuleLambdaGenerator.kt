package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils
import dev.sku20.helidon.ksp.annotation.CustomSerdeCatalogData
import java.io.InputStream

class RuleLambdaGenerator(
    private val function: KSFunctionDeclaration,
    private val imports: MutableSet<String>,
    private val params: MutableSet<String>,
    private val variables: MutableSet<String>,
    private val endpointCatalog: CustomSerdeCatalogData,
    private val w: CustomWriter,
) {

    fun write() = w.withRelativeIndent {
        val body = captureBody()
        writeVariables()
        w.write(body)
    }

    private val rulesVariables = mutableSetOf<String>()
    private fun writeVariables() = w.withRelativeIndent {
        for (variable in rulesVariables) writeLine("val $variable")
    }

    private fun captureBody(): InputStream = Utils.capturing { writer ->
        writer.setIndent(w.indent)
        RuleLambdaBodyGenerator(
            function,
            imports,
            params,
            variables,
            rulesVariables,
            endpointCatalog,
            writer
        ).write()
    }
}
