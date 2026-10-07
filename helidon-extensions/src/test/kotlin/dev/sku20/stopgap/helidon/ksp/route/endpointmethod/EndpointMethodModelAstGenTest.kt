package dev.sku20.stopgap.helidon.ksp.route.endpointmethod

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.ksp.route.customserdecatalog.CustomSerdeCatalogModel
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
    private val endpointCatalog = CustomSerdeCatalogModel("a.EndpointCatalog", null)

    @Test
    fun publicUnit() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p")
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, "public")

        assertThat(gen.rule()).isEqualTo(rule("get", endpointCall(), call("send", res)))
        assertThat(gen.catalogParam()).isNull()
        assertThat(gen.endpointVariables()).isEmpty()
    }

    @Test
    fun responseParamSkipsSend() {
        val model = EndpointMethodModel("fn", HttpMethod.POST, "/p", params = listOf(ResponseParamModel))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, "public")

        assertThat(gen.rule()).isEqualTo(rule("post", endpointCall(res)))
    }

    @Test
    fun authParam() {
        val authParam = AuthParamModel("a.User")
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p", authParamModel = authParam, params = listOf(authParam))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, "public")

        assertThat(gen.rule()).isEqualTo(
            rule("get", authVal(), authCheck("a.User"), endpointCall(AstLiteral("auth")), call("send", res))
        )
    }

    @Test
    fun defaultAuth() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p")
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, "a.Admin")

        assertThat(gen.rule()).isEqualTo(rule("get", authVal(), authCheck("a.Admin"), endpointCall(), call("send", res)))
    }

    @Test
    fun missingAuthThrows() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p")
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, null)

        assertThatThrownBy { gen.rule() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("a.Endpoint.fn")
    }

    @Test
    fun authBeforeSerde() {
        val authParam = AuthParamModel("a.User")
        val model = EndpointMethodModel(
            "fn",
            HttpMethod.GET,
            "/p",
            authParamModel = authParam,
            params = listOf(authParam),
            isResponseUnit = false,
        )
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, null)

        val body = (gen.rule().arguments[1] as AstLambda).content
        assertThat(body.take(3)).isEqualTo(listOf(authVal(), authCheck("a.User"), serVal("a_EndpointCatalog")))
    }

    @Test
    fun serializedResponse() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p", isResponseUnit = false)
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, "public")

        assertThat(gen.rule()).isEqualTo(
            rule(
                "get",
                serVal("a_EndpointCatalog"),
                AstAssignment(AstLiteral("resp"), null, VariableType.VAL, endpointCall()),
                call("contentType", call("headers", res), AstLiteral("ser.mediaType")),
                call("send", res, call("serialize", AstLiteral("ser"), AstLiteral("resp"))),
            )
        )
        assertThat(gen.catalogParam()).isEqualTo(AstParam(AstLiteral("a_EndpointCatalog"), AstType("a.EndpointCatalog")))
    }

    @Test
    fun body() {
        val body = BodyParamModel(TypeModel("a.Body"))
        val model = EndpointMethodModel("fn", HttpMethod.POST, "/p", params = listOf(body))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, "public")

        val deserVal = AstAssignment(
            AstLiteral("deser"),
            null,
            VariableType.VAL,
            call(
                "getDeserializer",
                AstLiteral("a_EndpointCatalog"),
                call("orElse", call("contentType", call("headers", req)), AstLiteral("null")),
            ),
        )
        val rule = gen.rule()
        val content = (rule.arguments[1] as AstLambda).content
        assertThat(content.first()).isEqualTo(deserVal)
        assertThat(gen.catalogParam()).isEqualTo(AstParam(AstLiteral("a_EndpointCatalog"), AstType("a.EndpointCatalog")))
    }

    @Test
    fun methodCatalogOverridesEndpoint() {
        val model = EndpointMethodModel(
            "fn",
            HttpMethod.GET,
            "/p",
            CustomSerdeCatalogModel("a.MethodCatalog", null),
            isResponseUnit = false,
        )
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, "public")

        assertThat(gen.catalogParam()).isEqualTo(AstParam(AstLiteral("a_MethodCatalog"), AstType("a.MethodCatalog")))
    }

    @Test
    fun endpointVariables() {
        val model = EndpointMethodModel("fn", HttpMethod.GET, "/p", params = listOf(HeaderParamModel("X-Id")))
        val gen = EndpointMethodModelAstGen(model, "a.Endpoint", endpointCatalog, "public")

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

    private fun serVal(catalog: String) = AstAssignment(
        AstLiteral("ser"),
        null,
        VariableType.VAL,
        call("getSerializer", AstLiteral(catalog), call("acceptedTypes", call("headers", req))),
    )

    private fun call(name: String, receiver: AstExpression, vararg arguments: AstExpression) =
        AstCall(AstLiteral(name), arguments.toList(), receiver)
}
