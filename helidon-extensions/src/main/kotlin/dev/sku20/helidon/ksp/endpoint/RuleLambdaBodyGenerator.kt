package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.annotation.CustomSerdeCatalogData
import dev.sku20.helidon.ksp.argument
import dev.sku20.helidon.ksp.findAnnotation
import dev.sku20.helidon.param.PathParam
import dev.sku20.helidon.param.QueryParam
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
    private val functionParams = function.parameters

    private val bodyKType = "${functionName}BodyKType"

    private fun writeFunctionCall() = w.withRelativeIndent {
        writeLine("val ${GeneratedNames.RESP} = ${GeneratedNames.ENDPOINT}.${functionName}(")
        withRelativeIndent(4) {
            for (param in functionParams) {
                val value = getParamValue(param)
                writeLine("$value,")
            }
        }
        writeLine(")")
    }

    private fun getParamValue(param: KSValueParameter): String {
        val type = param.type.resolve()
        val pathParam = param.findAnnotation(PathParam::class)
        val queryParam = param.findAnnotation(QueryParam::class)
        return when {
            type.declaration.qualifiedName!!.asString() == ServerRequest::class.qualifiedName -> GeneratedNames.REQ
            type.declaration.qualifiedName!!.asString() == ServerResponse::class.qualifiedName -> GeneratedNames.RES
            pathParam != null -> """${GeneratedNames.REQ}.path().pathParameters()["${pathParam.argument<String>("name")}"]"""
            queryParam != null -> """${GeneratedNames.REQ}.query().get("${queryParam.argument<String>("name")}")"""
            else -> bodyDeserialized(type)
        }
    }

    private fun bodyDeserialized(type: KSType): String {
        addSerdeCatalog()
        val requestBody = type.declaration
        imports.add(requestBody.qualifiedName!!.asString())
        rulesVariables.add("${GeneratedNames.DESER} = ${functionCatalog.paramName()}.getDeserializer(${GeneratedNames.REQ}.headers().contentType().orElse(null))")
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
            rulesVariables.add("${GeneratedNames.SER} = ${functionCatalog.paramName()}.getSerializer(${GeneratedNames.REQ}.headers().acceptedTypes())")
            writeLine("${GeneratedNames.RES}.headers().contentType(${GeneratedNames.SER}.mediaType)")
            writeLine("${GeneratedNames.RES}.send(${GeneratedNames.SER}.serialize(${GeneratedNames.RESP}))")
        } else if (!hasServerResponseParam()) {
            writeLine("${GeneratedNames.RES}.send()")
        }
    }

    private fun addSerdeCatalog() {
        imports.add("dev.sku20.helidon.serde.SerdeCatalog")
        params.add("${functionCatalog.asAnnotationString()} ${functionCatalog.paramName()}: SerdeCatalog")
    }

    private fun isUnit(type: KSType): Boolean =
        type.declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!

    private fun hasServerResponseParam(): Boolean =
        functionParams.any { it.type.resolve().declaration.qualifiedName!!.asString() == ServerResponse::class.qualifiedName }

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
