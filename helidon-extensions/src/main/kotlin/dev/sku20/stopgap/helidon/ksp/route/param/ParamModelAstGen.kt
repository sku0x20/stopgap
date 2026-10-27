package dev.sku20.stopgap.helidon.ksp.route.param

import dev.sku20.stopgap.codegen.ast.*

class ParamModelAstGen(
    private val model: ParamModel,
    private val functionName: String,
    private val reqMapperName: AstLiteral,
) {

    private val req = AstLiteral("req")
    private val res = AstLiteral("res")

    private val body = AstLiteral("body")

    fun argument(): AstExpression = when (model) {
        RequestParamModel -> req
        ResponseParamModel -> AstLiteral("res")
        is PathParamModel -> get(call("pathParameters", call("path", req)), AstStringLiteral(model.name))
        is QueryParamModel -> get(call("query", req), AstStringLiteral(model.name))
        is HeaderParamModel -> get(call("headers", req), headerName(model))
        is AuthParamModel -> AstLiteral("auth")
        is BodyParamModel -> if (model.isAutoCloseable) body else reqMapCall(model)
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

    fun isBodyParam(): Boolean = model is BodyParamModel

    fun closeableBody(): AstAssignment? {
        if (model !is BodyParamModel || !model.isAutoCloseable) return null
        return AstAssignment(body, null, VariableType.VAL, reqMapCall(model))
    }

    fun closeBody(): AstExpression {
        val close = AstCall(AstLiteral("close"), emptyList(), body)
        if (model is BodyParamModel && model.type.isNullable) {
            return AstIf(AstLiteral("${body.value} != null"), listOf(close))
        }
        return close
    }

    fun isSendingResponse(): Boolean = model == ResponseParamModel

    private fun reqMapCall(model: BodyParamModel): AstCall =
        AstCall(AstLiteral("map"), listOf(req, res, bodyKType), reqMapperName, listOf(toAstType(model.type)))

    // Built once per endpoint; typeOf inside the rule lambda would run on every request.
    private fun bodyKTypeVariable(model: BodyParamModel): AstAssignment =
        AstAssignment(
            bodyKType,
            null,
            VariableType.VAL,
            AstCall(AstLiteral("kotlin.reflect.typeOf"), emptyList(), null, listOf(toAstType(model.type)))
        )

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
