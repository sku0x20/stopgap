package dev.sku20.stopgap.helidon.ksp.route.endpoint

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.ksp.route.customreqmapper.CustomReqMapperModel
import dev.sku20.stopgap.helidon.ksp.route.customserdecatalog.CustomSerdeCatalogModel
import dev.sku20.stopgap.helidon.ksp.route.endpointmethod.EndpointMethodModel
import dev.sku20.stopgap.helidon.ksp.route.endpointmethod.EndpointMethodModelAstGen
import dev.sku20.stopgap.helidon.ksp.route.endpointmethod.HttpMethod
import dev.sku20.stopgap.helidon.ksp.route.param.BodyParamModel
import dev.sku20.stopgap.helidon.ksp.route.param.HeaderParamModel
import dev.sku20.stopgap.helidon.ksp.route.param.TypeModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EndpointModelAstGenTest {

    private val catalog = CustomSerdeCatalogModel("a.Catalog", null)
    private val reqMapper = CustomReqMapperModel("a.Mapper", null)
    private val endpointParam = AstParam(AstLiteral("endpoint"), AstType("a.Endpoint"))
    private val routesParam = AstParam(AstLiteral("routes"), AstType("io.helidon.webserver.http.HttpRouting.Builder"))

    @Test
    fun empty() {
        val model = EndpointModel("a.Endpoint", "/e", emptyList(), null, null, null)

        assertThat(EndpointModelAstGen(model, "public").function()).isEqualTo(
            AstFunction("registerRoutesFor", listOf(endpointParam, routesParam), listOf(register()))
        )
    }

    @Test
    fun methods() {
        val header = HeaderParamModel("X-Id")
        val first = EndpointMethodModel("first", HttpMethod.GET, "/a", params = listOf(header))
        val second = EndpointMethodModel("second", HttpMethod.POST, "/b", params = listOf(BodyParamModel(TypeModel("a.Body"))))
        val model = EndpointModel("a.Endpoint", "/e", listOf(first, second), catalog, reqMapper, null)

        val firstGen = EndpointMethodModelAstGen(first, "a.Endpoint", catalog, reqMapper, "public")
        val secondGen = EndpointMethodModelAstGen(second, "a.Endpoint", catalog, reqMapper, "public")
        assertThat(EndpointModelAstGen(model, "public").function()).isEqualTo(
            AstFunction(
                "registerRoutesFor",
                listOf(endpointParam, routesParam, AstParam(AstLiteral("a_Mapper"), AstType("a.Mapper"))),
                firstGen.endpointVariables() + secondGen.endpointVariables() + register(firstGen.rule(), secondGen.rule()),
            )
        )
    }

    @Test
    fun dedupesParamsAndVariables() {
        val header = HeaderParamModel("X-Id")
        val body = BodyParamModel(TypeModel("a.Body"))
        val first = EndpointMethodModel("first", HttpMethod.POST, "/a", params = listOf(header, body), isResponseUnit = false)
        val second = EndpointMethodModel("second", HttpMethod.POST, "/b", params = listOf(header, body), isResponseUnit = false)
        val model = EndpointModel("a.Endpoint", "/e", listOf(first, second), catalog, reqMapper, null)

        val function = EndpointModelAstGen(model, "public").function()

        val firstGen = EndpointMethodModelAstGen(first, "a.Endpoint", catalog, reqMapper, "public")
        val secondGen = EndpointMethodModelAstGen(second, "a.Endpoint", catalog, reqMapper, "public")
        assertThat(function.parameters)
            .containsExactly(endpointParam, routesParam, firstGen.catalogParam(), firstGen.reqMapperParam())
        assertThat(function.content.dropLast(1))
            .isEqualTo(firstGen.endpointVariables() + secondGen.endpointVariables().drop(1))
    }

    private fun register(vararg rules: AstExpression) = AstCall(
        AstLiteral("register"),
        listOf(AstStringLiteral("/e"), AstLambda(listOf(AstParam(AstLiteral("rules"))), rules.toList())),
        AstLiteral("routes"),
    )
}
