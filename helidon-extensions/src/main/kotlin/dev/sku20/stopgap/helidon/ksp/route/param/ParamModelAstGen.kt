package dev.sku20.stopgap.helidon.ksp.route.param

import dev.sku20.stopgap.codegen.ast.*

class ParamModelAstGen(
    private val model: ParamModel,
    private val functionName: String,
    private val catalogName: AstLiteral,
) {

    private val req = AstLiteral("req")

    fun argument(): AstExpression = when (model) {
        RequestParamModel -> req
        ResponseParamModel -> AstLiteral("res")
        is PathParamModel -> get(call("pathParameters", call("path", req)), AstStringLiteral(model.name))
        is QueryParamModel -> get(call("query", req), AstStringLiteral(model.name))
        is HeaderParamModel -> get(call("headers", req), headerName(model))
        is AuthParamModel -> AstLiteral("auth")
        is BodyParamModel -> bodyArgument(model)
    }

    fun endpointVariable(): AstAssignment? = when (model) {
        RequestParamModel, ResponseParamModel, is PathParamModel, is QueryParamModel, is AuthParamModel -> null
        is HeaderParamModel -> AstAssignment(
            headerName(model),
            null,
            VariableType.VAL,
            AstCall(AstLiteral("io.helidon.http.HeaderNames.create"), listOf(AstStringLiteral(model.name)))
        )
        is BodyParamModel -> bodyKTypeVariable(model)
    }

    fun ruleVariable(): AstAssignment? {
        if (model !is BodyParamModel) return null
        val contentType = AstCall(AstLiteral("orElse"), listOf(AstLiteral("null")), call("contentType", call("headers", req)))
        return AstAssignment(
            AstLiteral("deser"),
            null,
            VariableType.VAL,
            AstCall(AstLiteral("getDeserializer"), listOf(contentType), catalogName)
        )
    }

    private fun bodyArgument(model: BodyParamModel): AstCall {
        val bytes = AstCall(AstLiteral("readAllBytes"), emptyList(), call("inputStream", call("content", req)))
        val type = if (model.type.typeArguments.isEmpty()) AstLiteral("${model.type.fqn}::class") else bodyKType
        return AstCall(AstLiteral("deserialize"), listOf(bytes, type), AstLiteral("deser"))
    }

    private fun bodyKTypeVariable(model: BodyParamModel): AstAssignment? {
        if (model.type.typeArguments.isEmpty()) return null
        return AstAssignment(
            bodyKType,
            null,
            VariableType.VAL,
            AstCall(AstLiteral("kotlin.reflect.typeOf"), emptyList(), null, listOf(toAstType(model.type)))
        )
    }

    private val bodyKType = AstLiteral("${functionName}BodyKType")

    private fun headerName(model: HeaderParamModel) = AstLiteral("${model.name.replace('-', '_')}_header_name")

    private fun call(name: String, receiver: AstExpression) = AstCall(AstLiteral(name), emptyList(), receiver)

    private fun get(receiver: AstExpression, key: AstExpression) = AstCall(AstLiteral("get"), listOf(key), receiver)

    private fun toAstType(type: TypeModel): AstType {
        val typeArguments = mutableListOf<AstExpression>()
        for (typeArgument in type.typeArguments) {
            typeArguments.add(toAstType(typeArgument))
        }
        return AstType(type.fqn, typeArguments, type.isNullable)
    }
}
