package dev.sku20.stopgap.ir.ksp.creator

import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstExpression
import dev.sku20.stopgap.codegen.ast.AstLambda
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstStringLiteral
import dev.sku20.stopgap.codegen.ast.AstType
import dev.sku20.stopgap.ir.InstanceRegistry

class CreatorModelAstGen(private val model: CreatorModel) {

    private val registry = AstLiteral("registry")

    fun registration(): AstCall {
        val creator = AstLambda(emptyList(), listOf(callCreatorFunction()))
        if (model.qualifier != null) {
            return AstCall(
                AstLiteral("registerForQualifier"),
                listOf(AstStringLiteral(model.qualifier), creator),
                registry
            )
        }
        return AstCall(AstLiteral("registerForType"), listOf(creator), registry)
    }

    fun eagerCreation(): AstCall? {
        if (!model.eagerly) return null
        if (model.qualifier != null) {
            return AstCall(
                AstLiteral("getInstanceForQualifier"),
                listOf(AstStringLiteral(model.qualifier)),
                registry,
                listOf(AstType(model.returnType))
            )
        }
        return AstCall(
            AstLiteral("getInstanceForType"),
            emptyList(),
            registry,
            listOf(AstType(model.returnType))
        )
    }

    private fun callCreatorFunction(): AstCall {
        val arguments = mutableListOf<AstExpression>()
        for (parameter in model.parameters) {
            arguments.add(argument(parameter))
        }
        return AstCall(AstLiteral(model.functionName), arguments)
    }

    private fun argument(parameter: CreatorModel.Parameter): AstExpression {
        if (parameter.qualifier != null) {
            return AstCall(
                AstLiteral("getInstanceForQualifier"),
                listOf(AstStringLiteral(parameter.qualifier)),
                registry
            )
        }
        if (parameter.type == InstanceRegistry::class.qualifiedName!!) return registry
        return AstCall(
            AstLiteral("getInstanceForType"),
            emptyList(),
            registry,
            listOf(AstType(parameter.type))
        )
    }
}
