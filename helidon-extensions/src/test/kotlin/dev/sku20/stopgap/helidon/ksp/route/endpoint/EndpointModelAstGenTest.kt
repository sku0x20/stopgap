package dev.sku20.stopgap.helidon.ksp.route.endpoint

import dev.sku20.stopgap.codegen.ast.*
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
    private val endpointParam = AstParam(AstLiteral("endpoint"), AstType("a.Endpoint"))
    private val routesParam = AstParam(AstLiteral("routes"), AstType("io.helidon.webserver.http.HttpRouting.Builder"))

    @Test
    fun empty() {
        val model = EndpointModel("a.Endpoint", "/e", emptyList(), null)

        assertThat(EndpointModelAstGen(model, "public").function()).isEqualTo(
            AstFunction("registerRoutesFor", listOf(endpointParam, routesParam), listOf(register()))
        )
    }

    @Test
    fun methods() {
        val header = HeaderParamModel("X-Id")
        val first = EndpointMethodModel("first", HttpMethod.GET, "/a", params = listOf(header))
        val second = EndpointMethodModel("second", HttpMethod.POST, "/b", params = listOf(BodyParamModel(TypeModel("a.Body"))))
        val model = EndpointModel("a.Endpoint", "/e", listOf(first, second), catalog)

        val firstGen = EndpointMethodModelAstGen(first, "a.Endpoint", catalog, "public")
        val secondGen = EndpointMethodModelAstGen(second, "a.Endpoint", catalog, "public")
        assertThat(EndpointModelAstGen(model, "public").function()).isEqualTo(
            AstFunction(
                "registerRoutesFor",
                listOf(endpointParam, routesParam, AstParam(AstLiteral("a_Catalog"), AstType("a.Catalog"))),
                firstGen.endpointVariables() + register(firstGen.rule(), secondGen.rule()),
            )
        )
    }

    @Test
    fun dedupesParamsAndVariables() {
        val header = HeaderParamModel("X-Id")
        val body = BodyParamModel(TypeModel("a.Body"))
        val first = EndpointMethodModel("first", HttpMethod.POST, "/a", params = listOf(header, body))
        val second = EndpointMethodModel("second", HttpMethod.POST, "/b", params = listOf(header, body))
        val model = EndpointModel("a.Endpoint", "/e", listOf(first, second), catalog)

        val function = EndpointModelAstGen(model, "public").function()

        val firstGen = EndpointMethodModelAstGen(first, "a.Endpoint", catalog, "public")
        assertThat(function.parameters).containsExactly(endpointParam, routesParam, firstGen.catalogParam())
        assertThat(function.content.dropLast(1)).isEqualTo(firstGen.endpointVariables())
    }

    private fun register(vararg rules: AstExpression) = AstCall(
        AstLiteral("register"),
        listOf(AstStringLiteral("/e"), AstLambda(listOf(AstParam(AstLiteral("rules"))), rules.toList())),
        AstLiteral("routes"),
    )
}
