package dev.sku20.stopgap.helidon.ksp.route.endpointmethod

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.ksp.route.EndpointSymbolProcessor
import dev.sku20.stopgap.helidon.ksp.route.customreqmapper.CustomReqMapperModel
import dev.sku20.stopgap.helidon.ksp.route.customreqmapper.CustomReqMapperModelAstGen
import dev.sku20.stopgap.helidon.ksp.route.customresmapper.CustomResMapperModel
import dev.sku20.stopgap.helidon.ksp.route.customresmapper.CustomResMapperModelAstGen
import dev.sku20.stopgap.helidon.ksp.route.param.ParamModelAstGen
import io.helidon.http.HttpException
import io.helidon.http.Status

class EndpointMethodModelAstGen(
    private val model: EndpointMethodModel,
    private val endpointFqn: String,
    endpointReqMapper: CustomReqMapperModel?,
    endpointResMapper: CustomResMapperModel?,
    private val defaultAuthType: String?,
) {

    private val req = AstLiteral("req")
    private val res = AstLiteral("res")
    private val auth = AstLiteral("auth")
    private val resp = AstLiteral("resp")

    private val reqMapperGen = CustomReqMapperModelAstGen(model.customReqMapperModel ?: endpointReqMapper)
    private val resMapperGen = CustomResMapperModelAstGen(model.customResMapperModel ?: endpointResMapper)
    private val paramGens = model.params.map { ParamModelAstGen(it, model.name, reqMapperGen.name()) }

    fun rule(): AstCall = AstCall(
        AstLiteral(model.httpMethod.name.lowercase()),
        listOf(AstStringLiteral(model.path), AstLambda(listOf(AstParam(req), AstParam(res)), body())),
        AstLiteral("rules"),
    )

    fun endpointVariables(): List<AstAssignment> {
        val variables = mutableListOf<AstAssignment>()
        for (paramGen in paramGens) {
            val variable = paramGen.endpointVariable()
            if (variable != null) variables.add(variable)
        }
        return variables
    }

    fun resMapperParam(): AstParam? {
        if (model.isResponseUnit) return null
        return resMapperGen.param()
    }

    fun reqMapperParam(): AstParam? {
        if (!isUsingReqMapper()) return null
        return reqMapperGen.param()
    }

    private fun body(): List<AstExpression> {
        val body = mutableListOf<AstExpression>()
        body.addAll(authCheck())
        body.addAll(endpointCall())
        return body
    }

    private fun authCheck(): List<AstExpression> {
        val authType = authType() ?: return emptyList()
        val authentication = AstLiteral("${Authentication::class.qualifiedName}::class.java")
        val forbidden = AstCall(
            AstLiteral(HttpException::class.qualifiedName!!),
            listOf(AstStringLiteral("Forbidden"), AstLiteral("${Status::class.qualifiedName}.FORBIDDEN_403")),
        )
        return listOf(
            AstAssignment(
                auth,
                null,
                VariableType.VAL,
                call("orElse", call("get", call("context", req), authentication), AstLiteral("null")),
            ),
            AstIf(AstLiteral("${auth.value} !is $authType"), listOf(AstThrow(forbidden))),
        )
    }

    // null means public
    private fun authType(): String? {
        val authParam = model.authParamModel
        if (authParam != null) return authParam.type
        if (defaultAuthType == null) {
            throw IllegalArgumentException(
                "Endpoint method '$endpointFqn.${model.name}' omits an Authentication parameter, but '${EndpointSymbolProcessor.DEFAULT_AUTH_TYPE_OPTION}' is not configured. Either declare an Authentication parameter or configure the default type (e.g. \"public\" or an Authentication FQN)."
            )
        }
        if (defaultAuthType == "public") return null
        return defaultAuthType
    }

    private fun endpointCall(): List<AstExpression> {
        val arguments = mutableListOf<AstExpression>()
        for (paramGen in paramGens) {
            arguments.add(paramGen.argument())
        }
        val invocation = AstCall(AstLiteral(model.name), arguments, AstLiteral("endpoint"))
        if (!model.isResponseUnit) {
            return listOf(
                AstAssignment(resp, null, VariableType.VAL, invocation),
                call("map", resMapperGen.name(), req, res, resp),
            )
        }
        if (isSendingResponse()) return listOf(invocation)
        return listOf(invocation, call("send", res))
    }

    private fun isUsingReqMapper(): Boolean {
        for (paramGen in paramGens) {
            if (paramGen.isUsingReqMapper()) return true
        }
        return false
    }

    private fun isSendingResponse(): Boolean {
        for (paramGen in paramGens) {
            if (paramGen.isSendingResponse()) return true
        }
        return false
    }

    private fun call(name: String, receiver: AstExpression, vararg arguments: AstExpression) =
        AstCall(AstLiteral(name), arguments.toList(), receiver)
}
