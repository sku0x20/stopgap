package dev.sku20.stopgap.ir.ksp

import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstExpression
import dev.sku20.stopgap.codegen.ast.AstLambda
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstStringLiteral
import dev.sku20.stopgap.codegen.ast.AstType
import dev.sku20.stopgap.ir.InstanceRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreatorModelAstGenTest {

    private val registry = AstLiteral("registry")

    @Test
    fun registrationViaType() {
        val gen = CreatorModelAstGen(model(null, false, emptyList()))

        assertThat(gen.registration()).isEqualTo(
            AstCall(AstLiteral("registerForType"), listOf(creator(emptyList())), registry)
        )
    }

    @Test
    fun registrationViaQualifier() {
        val gen = CreatorModelAstGen(model("q", false, emptyList()))

        assertThat(gen.registration()).isEqualTo(
            AstCall(
                AstLiteral("registerForQualifier"),
                listOf(AstStringLiteral("q"), creator(emptyList())),
                registry
            )
        )
    }

    @Test
    fun argumentViaType() {
        val parameter = CreatorModel.Parameter("a.Dep", null)
        val gen = CreatorModelAstGen(model(null, false, listOf(parameter)))

        assertThat(gen.registration()).isEqualTo(
            AstCall(
                AstLiteral("registerForType"),
                listOf(creator(listOf(getInstanceForType("a.Dep")))),
                registry
            )
        )
    }

    @Test
    fun argumentViaQualifier() {
        val parameter = CreatorModel.Parameter("a.Dep", "dep")
        val gen = CreatorModelAstGen(model(null, false, listOf(parameter)))

        assertThat(gen.registration()).isEqualTo(
            AstCall(
                AstLiteral("registerForType"),
                listOf(
                    creator(
                        listOf(AstCall(AstLiteral("getInstanceForQualifier"), listOf(AstStringLiteral("dep")), registry))
                    )
                ),
                registry
            )
        )
    }

    @Test
    fun argumentRegistry() {
        val parameter = CreatorModel.Parameter(InstanceRegistry::class.qualifiedName!!, null)
        val gen = CreatorModelAstGen(model(null, false, listOf(parameter)))

        assertThat(gen.registration()).isEqualTo(
            AstCall(AstLiteral("registerForType"), listOf(creator(listOf(registry))), registry)
        )
    }

    @Test
    fun eagerCreation() {
        val gen = CreatorModelAstGen(model(null, true, emptyList()))

        assertThat(gen.eagerCreation()).isEqualTo(getInstanceForType("a.Foo"))
    }

    @Test
    fun eagerCreationNullWhenLazy() {
        val gen = CreatorModelAstGen(model(null, false, emptyList()))

        assertThat(gen.eagerCreation()).isNull()
    }

    private fun model(
        qualifier: String?,
        eagerly: Boolean,
        parameters: List<CreatorModel.Parameter>
    ) = CreatorModel("a.createFoo", "a.Foo", qualifier, eagerly, parameters)

    private fun creator(arguments: List<AstExpression>) =
        AstLambda(emptyList(), listOf(AstCall(AstLiteral("a.createFoo"), arguments)))

    private fun getInstanceForType(type: String) =
        AstCall(AstLiteral("getInstanceForType"), emptyList(), registry, listOf(AstType(type)))
}
