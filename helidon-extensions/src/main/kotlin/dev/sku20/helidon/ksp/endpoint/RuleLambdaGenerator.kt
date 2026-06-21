package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.serde.CustomSerde
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class RuleLambdaGenerator(
    private val function: KSFunctionDeclaration,
    private val endpoint: String,
    private val req: String,
    private val res: String,
) {

    private val functionName = function.simpleName.asString()

    private lateinit var w: CustomWriter
    private lateinit var imports: MutableSet<String>
    private lateinit var params: MutableSet<String>

    private lateinit var variables: MutableSet<String>
    fun write(
        w: CustomWriter,
        imports: MutableSet<String>,
        params: MutableSet<String>,
        variables: MutableSet<String>
    ) = w.withRelativeIndent {
        this@RuleLambdaGenerator.w = w
        this@RuleLambdaGenerator.params = params
        this@RuleLambdaGenerator.imports = imports
        this@RuleLambdaGenerator.variables = variables

        writeLambdaBody()
    }

    // in order
    private val functionParamsTypes = function.parameters.map { it.type.resolve() }

    private fun writeLambdaBody() = w.withRelativeIndent {
        writePrologue()
        writeFunctionCall()
        writeEpilogue()
    }

    private val defaultSerdeCatalog = "defaultSerdeCatalog"
    private val respVariable = "resp"
    private val bodyKType = "${functionName}BodyKType"

    // we can capture and required types from function call and epilogue.
    // but fine for now. loops not going to hurt much here.
    private fun writePrologue() = w.withRelativeIndent {
        addKTypeVariableIfValid()
    }

    private fun addKTypeVariableIfValid() = w.withRelativeIndent {
        val bodyType = getBodyTypeIfExists() ?: return@withRelativeIndent
        if (bodyType.isGeneric()) {
            imports.add("kotlin.reflect.typeOf")
            variables.add("$bodyKType = typeOf<${toFqnString(bodyType)}>()")
        }
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

    private fun writeEpilogue() = w.withRelativeIndent {
        writeSerializeIfValid()
    }

    private fun getParamValue(type: KSType) = when (type.declaration.qualifiedName!!.asString()) {
        ServerRequest::class.qualifiedName -> req
        ServerResponse::class.qualifiedName -> res
        else -> bodyDeserialized(type)
    }

    private fun bodyDeserialized(type: KSType): String {
        addSerdeCatalog()
        val requestBody = type.declaration
        imports.add(requestBody.qualifiedName!!.asString())
        return if (type.isGeneric()) "$defaultSerdeCatalog.get($req.headers().contentType().orElse(null)).deserialize($req.content().inputStream().readAllBytes(), $bodyKType)"
        else "$defaultSerdeCatalog.get($req.headers().contentType().orElse(null)).deserialize($req.content().inputStream().readAllBytes(), " +
            "${requestBody.simpleName.asString()}::class)"
    }

    private fun writeSerializeIfValid() = w.withRelativeIndent {
        val returnType = function.returnType!!.resolve()
        if (!isUnit(returnType)) {
            addSerdeCatalog()
            writeLine("$res.headers().contentType($defaultSerdeCatalog.get($req.headers().contentType().orElse(null)).mediaType)")
            writeLine("$res.send($defaultSerdeCatalog.get($req.headers().contentType().orElse(null)).serialize($respVariable))")
        } else if (!hasServerResponseParam()) {
            writeLine("$res.send()")
        }
    }

    private fun addSerdeCatalog() {
        imports.add("dev.sku20.helidon.serde.SerdeCatalog")
        imports.add("dev.sku20.helidon.serde.SerdeExtras")
        params.add("@RegistryQualifier(SerdeExtras.DEFAULT_CATALOG_QUALIFIER) $defaultSerdeCatalog: SerdeCatalog")
    }

    private fun isUnit(type: KSType): Boolean =
        type.declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!

    private fun hasServerResponseParam(): Boolean =
        functionParamsTypes.find { it.declaration.qualifiedName!!.asString() == ServerResponse::class.qualifiedName } != null

    private fun getBodyTypeIfExists() = functionParamsTypes.find {
        it.declaration.qualifiedName!!.asString() != ServerRequest::class.qualifiedName &&
            it.declaration.qualifiedName!!.asString() != ServerResponse::class.qualifiedName
    }

    // base we are adding as import
    // manual transversal/type resolution
    fun toFqnString(type: KSType): String {
        val base = type.declaration.qualifiedName!!.asString()
        if (type.arguments.isEmpty()) return base
        val args = type.arguments.joinToString(", ") { arg ->
            toFqnString(arg.type!!.resolve())
        }
        return "$base<$args>"
    }

    private fun customSerdeAnnotation(): KSAnnotation? =
        function.annotations.firstOrNull { it.shortName.asString() == CustomSerde::class.simpleName }

    private fun KSType.isGeneric(): Boolean = this.arguments.isNotEmpty()
}
