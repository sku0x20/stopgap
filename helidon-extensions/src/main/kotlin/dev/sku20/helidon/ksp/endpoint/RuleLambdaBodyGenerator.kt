package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.annotation.CustomSerdeCatalogData
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

class RuleLambdaBodyGenerator(
    private val function: KSFunctionDeclaration,
    private val imports: MutableSet<String>,
    private val params: MutableSet<String>,
    private val variables: MutableSet<String>,
    private val rulesVariables: MutableSet<String>,
    endpointCatalog: CustomSerdeCatalogData,
    private val w: CustomWriter,
) {

    private val functionName = function.simpleName.asString()
    private val functionCatalog = CustomSerdeCatalogData.fromOrDefault(
        function,
        endpointCatalog
    )

    fun write() = w.withRelativeIndent {
        writeFunctionCall()
        writeSerializeIfValid()
    }

    // in order
    private val functionParamsTypes = function.parameters.map { it.type.resolve() }

    private val bodyKType = "${functionName}BodyKType"

    private fun writeFunctionCall() = w.withRelativeIndent {
        writeLine("val ${GeneratedNames.RESP} = ${GeneratedNames.ENDPOINT}.${functionName}(")
        withRelativeIndent(4) {
            for (type in functionParamsTypes) {
                val value = getParamValue(type)
                writeLine("$value,")
            }
        }
        writeLine(")")
    }

    private fun getParamValue(type: KSType) = when (type.declaration.qualifiedName!!.asString()) {
        ServerRequest::class.qualifiedName -> GeneratedNames.REQ
        ServerResponse::class.qualifiedName -> GeneratedNames.RES
        else -> bodyDeserialized(type)
    }

    private fun bodyDeserialized(type: KSType): String {
        addSerdeCatalog()
        val requestBody = type.declaration
        imports.add(requestBody.qualifiedName!!.asString())
        rulesVariables.add("${GeneratedNames.DESER} = ${GeneratedNames.ENDPOINT_CATALOG}.getDeserializer(${GeneratedNames.REQ}.headers().contentType().orElse(null))")
        val typeParam: String
        if (type.isGeneric()) {
            imports.add("kotlin.reflect.typeOf")
            variables.add("$bodyKType = typeOf<${toFqnString(type)}>()")
            typeParam = bodyKType
        } else {
            typeParam = "${requestBody.simpleName.asString()}::class"
        }
        return "${GeneratedNames.DESER}.deserialize(${GeneratedNames.REQ}.content().inputStream().readAllBytes(), $typeParam)"
    }

    private fun writeSerializeIfValid() = w.withRelativeIndent {
        val returnType = function.returnType!!.resolve()
        if (!isUnit(returnType)) {
            addSerdeCatalog()
            rulesVariables.add("${GeneratedNames.SER} = ${GeneratedNames.ENDPOINT_CATALOG}.getSerializer(${GeneratedNames.REQ}.headers().acceptedTypes())")
            writeLine("${GeneratedNames.RES}.headers().contentType(${GeneratedNames.SER}.mediaType)")
            writeLine("${GeneratedNames.RES}.send(${GeneratedNames.SER}.serialize(${GeneratedNames.RESP}))")
        } else if (!hasServerResponseParam()) {
            writeLine("${GeneratedNames.RES}.send()")
        }
    }

    private fun addSerdeCatalog() {
        imports.add("dev.sku20.helidon.serde.SerdeCatalog")
        params.add("${functionCatalog.asAnnotationString()} ${GeneratedNames.ENDPOINT_CATALOG}: SerdeCatalog")
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

    private fun KSType.isGeneric(): Boolean = this.arguments.isNotEmpty()
}
