package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

class HandlerGenerator(
    private val function: KSFunctionDeclaration,
    private val endpointInstance: String,
    private val w: CustomWriter
) {

    fun write() = w.withRelativeIndent {
        val returnType = function.returnType!!.resolve()
        if (isUnit(returnType)) writeUnitResp()
    }

    fun writeUnitResp() = w.withRelativeIndent {
        writeLine("$endpointInstance.${function.simpleName.asString()}(")
        withRelativeIndent(4) {
            writeLine("req,")
            writeLine("res")
        }
        writeLine(")")
    }

    private fun isUnit(type: KSType): Boolean =
        type.declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!

}