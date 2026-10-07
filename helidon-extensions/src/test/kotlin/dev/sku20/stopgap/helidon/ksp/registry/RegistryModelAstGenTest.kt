package dev.sku20.stopgap.helidon.ksp.registry

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import io.helidon.webserver.http.HttpRouting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RegistryModelAstGenTest {

    private val registry = AstLiteral("registry")
    private val routes = AstLiteral("routes")

    private val initAuthentication = AstCall(
        AstLiteral("dev.sku20.stopgap.helidon.authentication.generated.initAuthentication"),
        listOf(getInstanceForType(AuthenticationResolver::class.qualifiedName!!), routes)
    )

    @Test
    fun packageName() {
        val file = RegistryModelAstGen(RegistryModel(emptyList()), "a.generated").file()

        assertThat(file.packageName).isEqualTo(AstLiteral("a.generated"))
    }

    @Test
    fun initHttpRouting() {
        val file = RegistryModelAstGen(RegistryModel(emptyList()), "a.generated").file()

        assertThat(file.content[0]).isEqualTo(
            AstFunction(
                "initHttpRouting",
                listOf(
                    AstParam(registry, AstType("dev.sku20.stopgap.ir.InstanceRegistry")),
                    AstParam(routes, AstType(HttpRouting.Builder::class.qualifiedName!!)),
                ),
                listOf(initAuthentication)
            )
        )
    }

    @Test
    fun registerRoutes() {
        val endpoint = RegistryModel.Endpoint(
            "a.FooEndpoint",
            listOf(
                RegistryModel.Param("a.FooCatalog", null),
                RegistryModel.Param("a.SerdeCatalog", "q")
            )
        )
        val file = RegistryModelAstGen(RegistryModel(listOf(endpoint)), "a.generated").file()

        assertThat((file.content[0] as AstFunction).content).containsExactly(
            initAuthentication,
            AstCall(
                AstLiteral("dev.sku20.stopgap.helidon.endpoint.generated.registerRoutesFor"),
                listOf(
                    getInstanceForType("a.FooEndpoint"),
                    routes,
                    getInstanceForType("a.FooCatalog"),
                    AstCall(AstLiteral("getInstanceForQualifier"), listOf(AstStringLiteral("q")), registry)
                )
            )
        )
    }

    private fun getInstanceForType(type: String): AstCall =
        AstCall(AstLiteral("getInstanceForType"), emptyList(), registry, listOf(AstType(type)))
}
