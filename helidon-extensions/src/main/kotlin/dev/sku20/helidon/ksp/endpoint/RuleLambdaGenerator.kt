package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils
import dev.sku20.helidon.serde.CustomSerde
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

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

        writeLambdaBody()
    }

    // in order
    private val functionParamsTypes = function.parameters.map { it.type.resolve() }

    private val functionCallGen = RuleLambdaFunctionCallGen(
        endpoint, functionName, functionParamsTypes, req, res, defaultSerde
    )

    private fun writeLambdaBody() = w.withRelativeIndent {
        writePrologue()
        functionCallGen.write(w, imports, params)
        writeEpilogue()
    }

    private fun writePrologue() = w.withRelativeIndent {

    }

    private fun writeEpilogue() = w.withRelativeIndent {
        writeSerializeIfValid()
    }

    private fun writeSerializeIfValid() = w.withRelativeIndent {
        val returnType = function.returnType!!.resolve()
        if (!isUnit(returnType)) {
            addSerde()
            writeLine("$defaultSerde.setHeaders($res.headers())")
            writeLine("$res.send($defaultSerde.serialize(${functionCallGen.respVariable}))")
        } else if (!hasServerResponseParam()) {
            writeLine("$res.send()")
        }
    }

    private fun addSerde() {
        imports.add("dev.sku20.helidon.serde.Serde")
        imports.add("dev.sku20.helidon.serde.SerdeExtras")
        params.add("@RegistryQualifier(SerdeExtras.DEFAULT_QUALIFIER) $defaultSerde: Serde")
    }

    private fun isUnit(type: KSType): Boolean =
        type.declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!

    private fun hasServerResponseParam(): Boolean =
        functionParamsTypes.find { it.declaration.qualifiedName!!.asString() == ServerResponse::class.qualifiedName } != null

    private fun customSerdeAnnotation(): KSAnnotation? =
        function.annotations.firstOrNull { it.shortName.asString() == CustomSerde::class.simpleName }

}

class RuleLambdaFunctionCallGen(
    private val endpoint: String,
    private val functionName: String,
    private val functionParamsTypes: List<KSType>,
    private val req: String,
    private val res: String,
    private val defaultSerde: String,
) {

    internal val respVariable = "resp"

    private lateinit var w: CustomWriter
    private lateinit var imports: MutableSet<String>
    private lateinit var params: MutableSet<String>

    fun write(w: CustomWriter, imports: MutableSet<String>, params: MutableSet<String>) {
        this.w = w
        this.imports = imports
        this.params = params
        writeFunctionCall()
    }

    private fun writeFunctionCall() = w.withRelativeIndent {
        writeLine("val $respVariable = $endpoint.${functionName}(")
        withRelativeIndent(4) {
            for (type in functionParamsTypes) {
                val value = getParamValue(type)
                writeLine("$value,")
            }
        }
        writeLine(")")
    }

    private fun getParamValue(type: KSType) = when (type.declaration.qualifiedName!!.asString()) {
        ServerRequest::class.qualifiedName -> req
        ServerResponse::class.qualifiedName -> res
        else -> bodyDeserialized(type)
    }

    private fun bodyDeserialized(type: KSType): String {
        addSerde()
        val requestBody = type.declaration
        imports.add(requestBody.qualifiedName!!.asString())
        return "$defaultSerde.deserialize($req.content().inputStream().readAllBytes(), " +
            "${requestBody.simpleName.asString()}::class)"
    }

    private fun addSerde() {
        imports.add("dev.sku20.helidon.serde.Serde")
        imports.add("dev.sku20.helidon.serde.SerdeExtras")
        params.add("@RegistryQualifier(SerdeExtras.DEFAULT_QUALIFIER) $defaultSerde: Serde")
    }

}
