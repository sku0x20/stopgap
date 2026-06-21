package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.serde.CustomSerdeCatalog
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class RuleLambdaBodyGenerator(
    private val function: KSFunctionDeclaration,
    private val endpoint: String,
    private val req: String,
    private val res: String,
    private val imports: MutableSet<String>,
    private val params: MutableSet<String>,
    private val variables: MutableSet<String>,
    private val rulesVariables: MutableSet<String>,
    private val w: CustomWriter,
) {

    private val functionName = function.simpleName.asString()

    fun write() = w.withRelativeIndent {
        writeFunctionCall()
        writeEpilogue()
    }

    // in order
    private val functionParamsTypes = function.parameters.map { it.type.resolve() }

    private val defaultSerdeCatalog = "defaultSerdeCatalog"
    private val respVariable = "resp"
    private val bodyKType = "${functionName}BodyKType"

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

    private val deserializer = "deser"
    private fun bodyDeserialized(type: KSType): String {
        addSerdeCatalog()
        val requestBody = type.declaration
        imports.add(requestBody.qualifiedName!!.asString())
        rulesVariables.add("$deserializer = $defaultSerdeCatalog.getDeserializer($req.headers().contentType().orElse(null))")
        val typeParam: String
        if (type.isGeneric()) {
            imports.add("kotlin.reflect.typeOf")
            variables.add("$bodyKType = typeOf<${toFqnString(type)}>()")
            typeParam = bodyKType
        } else {
            typeParam = "${requestBody.simpleName.asString()}::class"
        }
        return "${deserializer}.deserialize($req.content().inputStream().readAllBytes(), $typeParam)"
    }

    private val serializer = "ser"
    private fun writeSerializeIfValid() = w.withRelativeIndent {
        val returnType = function.returnType!!.resolve()
        if (!isUnit(returnType)) {
            addSerdeCatalog()
            rulesVariables.add("$serializer = $defaultSerdeCatalog.getSerializer($req.headers().acceptedTypes())")
            writeLine("$res.headers().contentType($serializer.mediaType)")
            writeLine("$res.send($serializer.serialize($respVariable))")
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
        function.annotations.firstOrNull { it.shortName.asString() == CustomSerdeCatalog::class.simpleName }

    private fun KSType.isGeneric(): Boolean = this.arguments.isNotEmpty()
}
