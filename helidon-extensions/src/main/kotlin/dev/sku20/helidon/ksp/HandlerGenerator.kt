package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration

class HandlerGenerator(
    private val function: KSFunctionDeclaration,
    private val endpointInstance: String,
    private val w: CustomWriter
) {

    fun write() = w.withRelativeIndent {
        writeLine("$endpointInstance.${function.simpleName.asString()}(")
        withRelativeIndent(4) {
            writeLine("req,")
            writeLine("res")
        }
        writeLine(")")
    }

}