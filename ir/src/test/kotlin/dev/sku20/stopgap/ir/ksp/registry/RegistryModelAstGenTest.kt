package dev.sku20.stopgap.ir.ksp.registry

import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstFunction
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstParam
import dev.sku20.stopgap.codegen.ast.AstType
import dev.sku20.stopgap.codegen.ast.Visibility
import dev.sku20.stopgap.ir.InstanceRegistry
import dev.sku20.stopgap.ir.ksp.creator.CreatorModel
import dev.sku20.stopgap.ir.ksp.creator.CreatorModelAstGen
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RegistryModelAstGenTest {

    private val registry = AstLiteral("registry")
    private val registryParam = AstParam(registry, AstType(InstanceRegistry::class.qualifiedName!!))

    private val lazyCreator = CreatorModel("a.createFoo", "a.Foo", null, false, emptyList())
    private val eagerCreator = CreatorModel("a.createBar", "a.Bar", null, true, emptyList())

    @Test
    fun packageName() {
        val file = RegistryModelAstGen(RegistryModel(emptyList()), "a.generated").file()

        assertThat(file.packageName).isEqualTo(AstLiteral("a.generated"))
    }

    @Test
    fun initRegistry() {
        val file = RegistryModelAstGen(RegistryModel(emptyList()), "a.generated").file()

        assertThat(file.content[0]).isEqualTo(
            AstFunction(
                "initRegistry",
                listOf(registryParam),
                listOf(
                    AstCall(AstLiteral("registerCreators"), listOf(registry)),
                    AstCall(AstLiteral("createInstancesEagerly"), listOf(registry))
                )
            )
        )
    }

    @Test
    fun registerCreators() {
        val model = RegistryModel(listOf(lazyCreator, eagerCreator))
        val file = RegistryModelAstGen(model, "a.generated").file()

        assertThat(file.content[1]).isEqualTo(
            AstFunction(
                "registerCreators",
                listOf(registryParam),
                listOf(
                    CreatorModelAstGen(lazyCreator).registration(),
                    CreatorModelAstGen(eagerCreator).registration()
                ),
                visibility = Visibility.PRIVATE
            )
        )
    }

    @Test
    fun createInstancesEagerlySkipsLazy() {
        val model = RegistryModel(listOf(lazyCreator, eagerCreator))
        val file = RegistryModelAstGen(model, "a.generated").file()

        assertThat(file.content[2]).isEqualTo(
            AstFunction(
                "createInstancesEagerly",
                listOf(registryParam),
                listOf(CreatorModelAstGen(eagerCreator).eagerCreation()!!),
                visibility = Visibility.PRIVATE
            )
        )
    }
}
