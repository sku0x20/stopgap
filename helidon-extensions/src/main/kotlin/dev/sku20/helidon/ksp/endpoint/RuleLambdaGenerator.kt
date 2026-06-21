package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.ksp.CustomWriter

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
        RuleLambdaBodyGenerator(function, endpoint, req, res, w, imports, params, variables).write()
    }
}
