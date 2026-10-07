package dev.sku20.stopgap.helidon.ksp.route.endpoint

import dev.sku20.stopgap.codegen.ast.*
import dev.sku20.stopgap.helidon.ksp.route.endpointmethod.EndpointMethodModelAstGen
import io.helidon.webserver.http.HttpRouting

class EndpointModelAstGen(
    private val model: EndpointModel,
    defaultAuthType: String?,
) {

    private val routes = AstLiteral("routes")
    private val rules = AstLiteral("rules")

    private val methodGens = model.methods.map {
        EndpointMethodModelAstGen(it, model.fqn, model.customSerdeCatalogModel, defaultAuthType)
    }

    fun function(): AstFunction {
        val content = mutableListOf<AstExpression>()
        content.addAll(variables())
        content.add(register())
        return AstFunction("registerRoutesFor", params(), content)
    }

    private fun params(): List<AstParam> {
        val params = mutableListOf(
            AstParam(AstLiteral("endpoint"), AstType(model.fqn)),
            AstParam(routes, AstType("${HttpRouting.Builder::class.qualifiedName}")),
        )
        for (methodGen in methodGens) {
            val param = methodGen.catalogParam()
            if (param != null && param !in params) params.add(param)
        }
        return params
    }

    private fun variables(): List<AstAssignment> {
        val variables = mutableListOf<AstAssignment>()
        for (methodGen in methodGens) {
            for (variable in methodGen.endpointVariables()) {
                if (variable !in variables) variables.add(variable)
            }
        }
        return variables
    }

    private fun register(): AstCall {
        val ruleCalls = mutableListOf<AstExpression>()
        for (methodGen in methodGens) {
            ruleCalls.add(methodGen.rule())
        }
        return AstCall(
            AstLiteral("register"),
            listOf(AstStringLiteral(model.path), AstLambda(listOf(AstParam(rules)), ruleCalls)),
            routes,
        )
    }
}
