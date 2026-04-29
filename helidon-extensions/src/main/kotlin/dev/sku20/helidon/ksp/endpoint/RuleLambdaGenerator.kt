package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.endpoint.CustomSerde
import dev.sku20.helidon.ksp.CustomWriter
import com.google.devtools.ksp.symbol.KSType

class RuleLambdaGenerator(
    private val function: KSFunctionDeclaration,
    private val endpoint: String,
    private val req: String,
    private val res: String,
    private val defaultSerde: String,
) {

    private val functionName = function.simpleName.asString()

    private lateinit var w: CustomWriter
    private lateinit var imports: MutableSet<String>
    private lateinit var params: MutableSet<String>

    fun write(
        w: CustomWriter,
        imports: MutableSet<String>,
        params: MutableSet<String>
    ) = w.withRelativeIndent {
        this@RuleLambdaGenerator.w = w
        this@RuleLambdaGenerator.params = params
        this@RuleLambdaGenerator.imports = imports

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
        imports.add("dev.sku20.helidon.serde.Serde")
        imports.add("dev.sku20.helidon.serde.NopSerde")
        params.add("$defaultSerde: Serde = NopSerde")

        writeLine("val resp = $endpoint.${functionName}(")
        withRelativeIndent(4) {
            writeLine("$req,")
            writeLine(res)
        }
        writeLine(")")
        writeLine("$res.send($defaultSerde.serialize(resp))")
    }

    private fun isUnit(type: KSType): Boolean =
        type.declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!

    private fun customSerdeAnnotation(): KSAnnotation? =
        function.annotations.firstOrNull { it.shortName.asString() == CustomSerde::class.simpleName }

    private fun validatedCustomSerde(): KSAnnotation? {
        val annotation = customSerdeAnnotation() ?: return null
        val qualifier = annotation.arguments.first { it.name?.getShortName() == "qualifier" }.value as String
        val clazz = annotation.arguments.first { it.name?.getShortName() == "clazz" }.value
        val hasQualifier = qualifier.isNotEmpty()
        val hasClazz = clazz.toString() != Unit::class.qualifiedName!!
        check(!(hasQualifier && hasClazz)) {
            "CustomSerde on ${function.qualifiedName?.asString()}: set qualifier or clazz, not both"
        }
        return annotation
    }

}
