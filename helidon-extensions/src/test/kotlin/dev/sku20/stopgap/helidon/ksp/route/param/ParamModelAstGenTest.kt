package dev.sku20.stopgap.helidon.ksp.route.param

import dev.sku20.stopgap.codegen.ast.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ParamModelAstGenTest {

    private val req = AstLiteral("req")
    private val catalog = AstLiteral("a_Catalog")

    @Test
    fun request() {
        val gen = ParamModelAstGen(RequestParamModel, "fn", catalog)

        assertThat(gen.isUsingCatalog()).isFalse()
        assertThat(gen.isSendingResponse()).isFalse()
        assertThat(gen.argument()).isEqualTo(req)
        assertThat(gen.endpointVariable()).isNull()
        assertThat(gen.ruleVariable()).isNull()
    }

    @Test
    fun response() {
        val gen = ParamModelAstGen(ResponseParamModel, "fn", catalog)

        assertThat(gen.isSendingResponse()).isTrue()
        assertThat(gen.argument()).isEqualTo(AstLiteral("res"))
        assertThat(gen.endpointVariable()).isNull()
        assertThat(gen.ruleVariable()).isNull()
    }

    @Test
    fun path() {
        val gen = ParamModelAstGen(PathParamModel("id"), "fn", catalog)

        assertThat(gen.argument()).isEqualTo(
            get(call("pathParameters", call("path", req)), AstStringLiteral("id"))
        )
        assertThat(gen.endpointVariable()).isNull()
        assertThat(gen.ruleVariable()).isNull()
    }

    @Test
    fun query() {
        val gen = ParamModelAstGen(QueryParamModel("q"), "fn", catalog)

        assertThat(gen.argument()).isEqualTo(get(call("query", req), AstStringLiteral("q")))
        assertThat(gen.endpointVariable()).isNull()
        assertThat(gen.ruleVariable()).isNull()
    }

    @Test
    fun header() {
        val gen = ParamModelAstGen(HeaderParamModel("X-Id"), "fn", catalog)

        assertThat(gen.argument()).isEqualTo(get(call("headers", req), AstLiteral("X_Id_header_name")))
        assertThat(gen.ruleVariable()).isNull()
        assertThat(gen.endpointVariable()).isEqualTo(
            AstAssignment(
                AstLiteral("X_Id_header_name"),
                null,
                VariableType.VAL,
                AstCall(AstLiteral("io.helidon.http.HeaderNames.create"), listOf(AstStringLiteral("X-Id")))
            )
        )
    }

    @Test
    fun auth() {
        val gen = ParamModelAstGen(AuthParamModel("a.User"), "fn", catalog)

        assertThat(gen.argument()).isEqualTo(AstLiteral("auth"))
        assertThat(gen.endpointVariable()).isNull()
        assertThat(gen.ruleVariable()).isNull()
    }

    @Test
    fun body() {
        val gen = ParamModelAstGen(BodyParamModel(TypeModel("a.Body")), "fn", catalog)

        assertThat(gen.isUsingCatalog()).isTrue()
        assertThat(gen.argument()).isEqualTo(deserialize(AstLiteral("a.Body::class")))
        assertThat(gen.endpointVariable()).isNull()
        assertThat(gen.ruleVariable()).isEqualTo(deserVal)
    }

    @Test
    fun genericBody() {
        val type = TypeModel("kotlin.collections.List", listOf(TypeModel("a.Body", isNullable = true)))
        val gen = ParamModelAstGen(BodyParamModel(type), "fn", catalog)

        assertThat(gen.argument()).isEqualTo(deserialize(AstLiteral("fnBodyKType")))
        assertThat(gen.endpointVariable()).isEqualTo(
            AstAssignment(
                AstLiteral("fnBodyKType"),
                null,
                VariableType.VAL,
                AstCall(
                    AstLiteral("kotlin.reflect.typeOf"),
                    emptyList(),
                    null,
                    listOf(AstType("kotlin.collections.List", listOf(AstType("a.Body", isNullable = true))))
                )
            )
        )
        assertThat(gen.ruleVariable()).isEqualTo(deserVal)
    }

    private val deserVal = AstAssignment(
        AstLiteral("deser"),
        null,
        VariableType.VAL,
        AstCall(
            AstLiteral("getDeserializer"),
            listOf(AstCall(AstLiteral("orElse"), listOf(AstLiteral("null")), call("contentType", call("headers", req)))),
            catalog
        )
    )

    private fun deserialize(type: AstExpression) = AstCall(
        AstLiteral("deserialize"),
        listOf(AstCall(AstLiteral("readAllBytes"), emptyList(), call("inputStream", call("content", req))), type),
        AstLiteral("deser")
    )

    private fun call(name: String, receiver: AstExpression) = AstCall(AstLiteral(name), emptyList(), receiver)

    private fun get(receiver: AstExpression, key: AstExpression) = AstCall(AstLiteral("get"), listOf(key), receiver)
}
