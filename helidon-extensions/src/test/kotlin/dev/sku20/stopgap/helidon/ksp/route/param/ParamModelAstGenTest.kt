package dev.sku20.stopgap.helidon.ksp.route.param

import dev.sku20.stopgap.codegen.ast.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ParamModelAstGenTest {

    private val req = AstLiteral("req")
    private val reqMapper = AstLiteral("a_Mapper")

    @Test
    fun request() {
        val gen = ParamModelAstGen(RequestParamModel, "fn", reqMapper)

        assertThat(gen.isBodyParam()).isFalse()
        assertThat(gen.isSendingResponse()).isFalse()
        assertThat(gen.argument()).isEqualTo(req)
        assertThat(gen.endpointVariable()).isNull()
    }

    @Test
    fun response() {
        val gen = ParamModelAstGen(ResponseParamModel, "fn", reqMapper)

        assertThat(gen.isSendingResponse()).isTrue()
        assertThat(gen.argument()).isEqualTo(AstLiteral("res"))
        assertThat(gen.endpointVariable()).isNull()
    }

    @Test
    fun path() {
        val gen = ParamModelAstGen(PathParamModel("id"), "fn", reqMapper)

        assertThat(gen.argument()).isEqualTo(
            get(call("pathParameters", call("path", req)), AstStringLiteral("id"))
        )
        assertThat(gen.endpointVariable()).isNull()
    }

    @Test
    fun query() {
        val gen = ParamModelAstGen(QueryParamModel("q"), "fn", reqMapper)

        assertThat(gen.argument()).isEqualTo(get(call("query", req), AstStringLiteral("q")))
        assertThat(gen.endpointVariable()).isNull()
    }

    @Test
    fun header() {
        val gen = ParamModelAstGen(HeaderParamModel("X-Id"), "fn", reqMapper)

        assertThat(gen.argument()).isEqualTo(get(call("headers", req), AstLiteral("X_Id_header_name")))
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
        val gen = ParamModelAstGen(AuthParamModel("a.User"), "fn", reqMapper)

        assertThat(gen.argument()).isEqualTo(AstLiteral("auth"))
        assertThat(gen.endpointVariable()).isNull()
    }

    @Test
    fun body() {
        val gen = ParamModelAstGen(BodyParamModel(TypeModel("a.Body")), "fn", reqMapper)

        assertThat(gen.isBodyParam()).isTrue()
        assertThat(gen.argument()).isEqualTo(map(AstType("a.Body")))
        assertThat(gen.endpointVariable()).isEqualTo(kTypeVal(AstType("a.Body")))
        assertThat(gen.reqBody()).isNull()
    }

    @Test
    fun closeableBody() {
        val gen = ParamModelAstGen(BodyParamModel(TypeModel("a.Body"), true), "fn", reqMapper)

        assertThat(gen.argument()).isEqualTo(AstLiteral("body"))
        assertThat(gen.reqBody())
            .isEqualTo(AstAssignment(AstLiteral("body"), null, VariableType.VAL, map(AstType("a.Body"))))
        assertThat(gen.closeBody()).isEqualTo(AstCall(AstLiteral("close"), emptyList(), AstLiteral("body")))
    }

    @Test
    fun nullableCloseableBody() {
        val gen = ParamModelAstGen(BodyParamModel(TypeModel("a.Body", isNullable = true), true), "fn", reqMapper)

        assertThat(gen.closeBody()).isEqualTo(
            AstIf(
                AstLiteral("body != null"),
                listOf(AstCall(AstLiteral("close"), emptyList(), AstLiteral("body")))
            )
        )
    }

    @Test
    fun genericBody() {
        val type = TypeModel("kotlin.collections.List", listOf(TypeModel("a.Body", isNullable = true)))
        val gen = ParamModelAstGen(BodyParamModel(type), "fn", reqMapper)

        val astType = AstType("kotlin.collections.List", listOf(AstType("a.Body", isNullable = true)))
        assertThat(gen.argument()).isEqualTo(map(astType))
        assertThat(gen.endpointVariable()).isEqualTo(kTypeVal(astType))
    }

    private fun map(type: AstType) =
        AstCall(AstLiteral("map"), listOf(req, AstLiteral("res"), AstLiteral("fnBodyKType")), reqMapper, listOf(type))

    private fun kTypeVal(type: AstType) = AstAssignment(
        AstLiteral("fnBodyKType"),
        null,
        VariableType.VAL,
        AstCall(AstLiteral("kotlin.reflect.typeOf"), emptyList(), null, listOf(type))
    )

    private fun call(name: String, receiver: AstExpression) = AstCall(AstLiteral(name), emptyList(), receiver)

    private fun get(receiver: AstExpression, key: AstExpression) = AstCall(AstLiteral("get"), listOf(key), receiver)
}
