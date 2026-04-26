package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

class RuleLambdaGenerator(
    private val function: KSFunctionDeclaration,
    private val endpoint: String,
    private val req: String,
    private val res: String,
    private val w: CustomWriter
) {

    private val functionName = function.simpleName.asString()

    fun write() = w.withRelativeIndent {
        val returnType = function.returnType!!.resolve()
        if (isUnit(returnType)) writeUnitResp()
        else writeResp()
    }

    private fun writeUnitResp() = w.withRelativeIndent {
        writeLine("$endpoint.${functionName}(")
        withRelativeIndent(4) {
            writeLine("$req,")
            writeLine(res)
        }
        writeLine(")")
    }

    private fun writeResp() = w.withRelativeIndent {
        writeLine("val resp = $endpoint.${functionName}(")
        withRelativeIndent(4) {
            writeLine("$req,")
            writeLine(res)
        }
        writeLine(")")
        writeLine("$res.send(serde.serialize(resp))")
    }

    private fun isUnit(type: KSType): Boolean =
        type.declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!

}