package dev.sku20.stopgap.helidon.ksp.route.endpointmethod

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.ksp.route.customreqmapper.CustomReqMapperModel
import dev.sku20.stopgap.helidon.ksp.route.customresmapper.CustomResMapperModel
import dev.sku20.stopgap.helidon.ksp.route.param.AuthParamModel
import dev.sku20.stopgap.helidon.ksp.route.param.BodyParamModel
import dev.sku20.stopgap.helidon.ksp.route.param.HeaderParamModel
import dev.sku20.stopgap.helidon.ksp.route.param.ResponseParamModel
import dev.sku20.stopgap.helidon.ksp.route.param.TypeModel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EndpointMethodModelAstGenTest {

    private val req = AstLiteral("req")
    private val res = AstLiteral("res")
    private val endpointReqMapper = CustomReqMapperModel("a.EndpointMapper", null)
    private val endpointResMapper = CustomResMapperModel("a.EndpointResMapper", null)

    @Test
    fun publicUnit() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p")
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        assertThat(gen.rule()).isEqualTo(rule("get", endpointCall(), call("send", res)))
        assertThat(gen.reqMapperParam()).isNull()
        assertThat(gen.resMapperParam()).isNull()
        assertThat(gen.endpointVariables()).isEmpty()
    }

    @Test
    fun responseParamSkipsSend() {
        val model = EndpointMethodModel("fn", HttpMethod.POST, "/p", params = listOf(ResponseParamModel))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        assertThat(gen.rule()).isEqualTo(rule("post", endpointCall(res)))
    }

    @Test
    fun authParam() {
        val authParam = AuthParamModel("a.User")
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p", authParamModel = authParam, params = listOf(authParam))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        assertThat(gen.rule()).isEqualTo(
            rule("get", authVal(), authCheck("a.User"), endpointCall(AstLiteral("auth")), call("send", res))
        )
    }

    @Test
    fun defaultAuth() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p")
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "a.Admin")

        assertThat(gen.rule()).isEqualTo(rule("get", authVal(), authCheck("a.Admin"), endpointCall(), call("send", res)))
    }

    @Test
    fun missingAuthThrows() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p")
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, null)

        assertThatThrownBy { gen.rule() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("a.Endpoint.fn")
    }

    @Test
    fun authBeforeResponse() {
        val authParam = AuthParamModel("a.User")
        val model = EndpointMethodModel(
            "fn",
            HttpMethod.GET,
            "/p",
            authParamModel = authParam,
            params = listOf(authParam),
            isResponseUnit = false,
        )
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, null)

        assertThat(gen.rule()).isEqualTo(rule("get", authVal(), authCheck("a.User"), respVal(AstLiteral("auth")), mapResp("a_EndpointResMapper")))
    }

    @Test
    fun mappedResponse() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p", isResponseUnit = false)
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        assertThat(gen.rule()).isEqualTo(
            rule("get", respVal(), mapResp("a_EndpointResMapper"))
        )
        assertThat(gen.resMapperParam()).isEqualTo(AstParam(AstLiteral("a_EndpointResMapper"), AstType("a.EndpointResMapper")))
    }

    @Test
    fun body() {
        val body = BodyParamModel(TypeModel("a.Body"))
        val model = EndpointMethodModel("fn", HttpMethod.POST, "/p", params = listOf(body))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        val map = AstCall(
            AstLiteral("map"),
            listOf(req, res, AstLiteral("fnBodyKType")),
            AstLiteral("a_EndpointMapper"),
            listOf(AstType("a.Body")),
        )
        assertThat(gen.rule()).isEqualTo(
            rule(
                "post",
                AstAssignment(AstLiteral("body"), null, VariableType.VAL, map),
                endpointCall(AstLiteral("body")),
                call("send", res),
            )
        )
        assertThat(gen.resMapperParam()).isNull()
        assertThat(gen.reqMapperParam()).isEqualTo(AstParam(AstLiteral("a_EndpointMapper"), AstType("a.EndpointMapper")))
    }

    @Test
    fun closesAutoCloseableBody() {
        val body = BodyParamModel(TypeModel("a.Body"), true)
        val model = EndpointMethodModel("fn", HttpMethod.POST, "/p", params = listOf(body), isResponseUnit = false)
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        val map = AstCall(
            AstLiteral("map"),
            listOf(req, res, AstLiteral("fnBodyKType")),
            AstLiteral("a_EndpointMapper"),
            listOf(AstType("a.Body")),
        )
        assertThat(gen.rule()).isEqualTo(
            rule(
                "post",
                AstAssignment(AstLiteral("body"), null, VariableType.VAL, map),
                AstTry(
                    listOf(respVal(AstLiteral("body")), mapResp("a_EndpointResMapper")),
                    emptyList(),
                    listOf(call("close", AstLiteral("body"))),
                ),
            )
        )
    }

    @Test
    fun closesAutoCloseableBodyAfterSend() {
        val body = BodyParamModel(TypeModel("a.Body"), true)
        val model = EndpointMethodModel("fn", HttpMethod.POST, "/p", params = listOf(body))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        val content = (gen.rule().arguments[1] as AstLambda).content
        assertThat(content[1]).isEqualTo(
            AstTry(
                listOf(endpointCall(AstLiteral("body")), call("send", res)),
                emptyList(),
                listOf(call("close", AstLiteral("body"))),
            )
        )
    }

    @Test
    fun methodReqMapperOverridesEndpoint() {
        val body = BodyParamModel(TypeModel("a.Body"))
        val model = EndpointMethodModel(
            "fn",
            HttpMethod.POST,
            "/p",
            customReqMapperModel = CustomReqMapperModel("a.MethodMapper", null),
            params = listOf(body),
        )
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        assertThat(gen.reqMapperParam()).isEqualTo(AstParam(AstLiteral("a_MethodMapper"), AstType("a.MethodMapper")))
    }

    @Test
    fun methodResMapperOverridesEndpoint() {
        val model = EndpointMethodModel(
            "fn",
            HttpMethod.GET,
            "/p",
            customResMapperModel = CustomResMapperModel("a.MethodResMapper", null),
            isResponseUnit = false,
        )
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        assertThat(gen.resMapperParam()).isEqualTo(AstParam(AstLiteral("a_MethodResMapper"), AstType("a.MethodResMapper")))
    }

    @Test
    fun endpointVariables() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p", params = listOf(HeaderParamModel("X-Id")))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointReqMapper, endpointResMapper, "public")

        assertThat(gen.endpointVariables()).containsExactly(
            AstAssignment(
                AstLiteral("X_Id_header_name"),
                null,
                VariableType.VAL,
                AstCall(AstLiteral("io.helidon.http.HeaderNames.create"), listOf(AstStringLiteral("X-Id")))
            )
        )
    }

    private fun rule(method: String, vararg content: AstExpression) = AstCall(
        AstLiteral(method),
        listOf(AstStringLiteral("/p"), AstLambda(listOf(AstParam(req), AstParam(res)), content.toList())),
        AstLiteral("rules"),
    )

    private fun endpointCall(vararg arguments: AstExpression) =
        AstCall(AstLiteral("fn"), arguments.toList(), AstLiteral("endpoint"))

    private fun authVal() = AstAssignment(
        AstLiteral("auth"),
        null,
        VariableType.VAL,
        call(
            "orElse",
            call(
                "get",
                call("context", req),
                AstLiteral("dev.sku20.stopgap.helidon.authentication.Authentication::class.java"),
            ),
            AstLiteral("null"),
        ),
    )

    private fun authCheck(type: String) = AstIf(
        AstLiteral("auth !is $type"),
        listOf(
            AstThrow(
                AstCall(
                    AstLiteral("io.helidon.http.HttpException"),
                    listOf(AstStringLiteral("Forbidden"), AstLiteral("io.helidon.http.Status.FORBIDDEN_403")),
                )
            )
        ),
    )

    private fun respVal(vararg arguments: AstExpression) =
        AstAssignment(AstLiteral("resp"), null, VariableType.VAL, endpointCall(*arguments))

    private fun mapResp(resMapper: String) = call("map", AstLiteral(resMapper), req, res, AstLiteral("resp"))

    private fun call(name: String, receiver: AstExpression, vararg arguments: AstExpression) =
        AstCall(AstLiteral(name), arguments.toList(), receiver)
}
