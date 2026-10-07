package dev.sku20.stopgap.ir.ksp.registry

import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstExpression
import dev.sku20.stopgap.codegen.ast.AstFile
import dev.sku20.stopgap.codegen.ast.AstFunction
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstParam
import dev.sku20.stopgap.codegen.ast.AstType
import dev.sku20.stopgap.codegen.ast.Visibility
import dev.sku20.stopgap.ir.InstanceRegistry
import dev.sku20.stopgap.ir.ksp.creator.CreatorModelAstGen

class RegistryModelAstGen(
    private val model: RegistryModel,
    private val packageName: String
) {

    private val registry = AstLiteral("registry")
    private val registerCreators = "registerCreators"
    private val createInstancesEagerly = "createInstancesEagerly"

    fun file(): AstFile = AstFile(
        AstLiteral(packageName),
        listOf(initRegistryFn(), registerCreatorsFn(), createInstancesEagerlyFn())
    )

    private fun initRegistryFn(): AstFunction = AstFunction(
        "initRegistry",
        listOf(registryParam()),
        listOf(
            AstCall(AstLiteral(registerCreators), listOf(registry)),
            AstCall(AstLiteral(createInstancesEagerly), listOf(registry))
        )
    )

    private fun registerCreatorsFn(): AstFunction {
        val content = mutableListOf<AstExpression>()
        for (creator in model.creators) {
            content.add(CreatorModelAstGen(creator).registration())
        }
        return AstFunction(
            registerCreators,
            listOf(registryParam()),
            content,
            visibility = Visibility.PRIVATE
        )
    }

    private fun createInstancesEagerlyFn(): AstFunction {
        val content = mutableListOf<AstExpression>()
        for (creator in model.creators) {
            val eagerCreation = CreatorModelAstGen(creator).eagerCreation()
            if (eagerCreation != null) content.add(eagerCreation)
        }
        return AstFunction(
            createInstancesEagerly,
            listOf(registryParam()),
            content,
            visibility = Visibility.PRIVATE
        )
    }

    private fun registryParam(): AstParam =
        AstParam(registry, AstType(InstanceRegistry::class.qualifiedName!!))
}
