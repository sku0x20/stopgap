package dev.sku20.stopgap.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.ksp.CustomWriter
import dev.sku20.stopgap.helidon.ksp.annotation.CustomSerdeCatalogData
import io.helidon.http.HttpException
import io.helidon.http.Status

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
        writeAuth()
        writeFunctionCall()
        writeSerializeIfValid()
    }

    // in order
    private val endpointParams = function.parameters.map { EndpointParam.from(it) }
    private val hasServerResponseParam = endpointParams.any { it is EndpointParam.Response }
    private val authParam = endpointParams.filterIsInstance<EndpointParam.Auth>().firstOrNull()

    private fun writeAuth() = w.withRelativeIndent {
        val auth = authParam ?: return@withRelativeIndent
        imports.add(Authentication::class.qualifiedName!!)
        imports.add(HttpException::class.qualifiedName!!)
        imports.add(Status::class.qualifiedName!!)
        val targetTypeDecl = auth.type.declaration
        imports.add(targetTypeDecl.qualifiedName!!.asString())
        val targetTypeName = targetTypeDecl.simpleName.asString()
        writeLine("val ${GeneratedNames.AUTH} = ${GeneratedNames.REQ}.context().get(${Authentication::class.simpleName}::class.java).orElse(null)")
        writeLine("if (${GeneratedNames.AUTH} !is $targetTypeName) {")
        withRelativeIndent(4) {
            writeLine("throw ${HttpException::class.simpleName}(\"Forbidden\", ${Status::class.simpleName}.FORBIDDEN_403)")
        }
        writeLine("}")
    }

    private val bodyKType = "${functionName}BodyKType"

    private fun writeFunctionCall() = w.withRelativeIndent {
        writeLine("val ${GeneratedNames.RESP} = ${GeneratedNames.ENDPOINT}.${functionName}(")
        withRelativeIndent(4) {
            for (param in endpointParams) {
                val value = getParamValue(param)
                writeLine("$value,")
            }
        }
        writeLine(")")
    }

    private fun getParamValue(param: EndpointParam): String = when (param) {
        is EndpointParam.Request -> GeneratedNames.REQ
        is EndpointParam.Response -> GeneratedNames.RES
        is EndpointParam.Path -> """${GeneratedNames.REQ}.path().pathParameters()["${param.name}"]"""
        is EndpointParam.Query -> """${GeneratedNames.REQ}.query().get("${param.name}")"""
        is EndpointParam.Header -> headerParamValue(param.name)
        is EndpointParam.Auth -> GeneratedNames.AUTH
        is EndpointParam.Body -> bodyDeserialized(param.type)
    }

    private fun headerParamValue(name: String): String {
        imports.add("io.helidon.http.HeaderNames")
        val varName = "${name.replace('-', '_')}_header_name"
        variables.add("$varName = HeaderNames.create(\"$name\")")
        return "${GeneratedNames.REQ}.headers().get($varName)"
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
        } else if (!hasServerResponseParam) {
            writeLine("${GeneratedNames.RES}.send()")
        }
    }

    private fun addSerdeCatalog() {
        imports.add("dev.sku20.stopgap.helidon.serde.SerdeCatalog")
        params.add("${functionCatalog.asAnnotationString()} ${functionCatalog.paramName()}: SerdeCatalog")
    }

    private fun isUnit(type: KSType): Boolean =
        type.declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!

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
