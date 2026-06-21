package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils
import java.io.InputStream

class RuleLambdaGenerator(
    private val function: KSFunctionDeclaration,
    private val endpoint: String,
    private val req: String,
    private val res: String,
    private val w: CustomWriter,
    private val imports: MutableSet<String>,
    private val params: MutableSet<String>,
    private val variables: MutableSet<String>,
) {

    fun write() = w.withRelativeIndent {
        val body = captureBody()
        w.write(body)
    }

    private fun captureBody(): InputStream = Utils.capturing { writer ->
        writer.setIndent(w.indent)
        RuleLambdaBodyGenerator(function, endpoint, req, res, writer, imports, params, variables).write()
    }
}
