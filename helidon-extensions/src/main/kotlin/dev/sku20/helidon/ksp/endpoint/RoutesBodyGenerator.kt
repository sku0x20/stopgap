package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils
import dev.sku20.helidon.ksp.endpoint.annotation.CustomSerdeCatalogData
import dev.sku20.helidon.ksp.endpoint.annotation.EndpointData
import dev.sku20.helidon.ksp.endpoint.annotation.RouteData

class RoutesBodyGenerator(
    private val endpointClazz: KSClassDeclaration,
    private val endpointParam: String,
    private val routesParam: String,
    private val imports: MutableSet<String>,
    private val params: MutableSet<String>,
    private val variables: MutableSet<String>,
    private val w: CustomWriter,
) {
    private val endpointAnnotation = EndpointData.from(endpointClazz)
    private val endpointCatalog = CustomSerdeCatalogData.from(endpointClazz)

    private val rulesField = "rules"

    fun write() = w.withRelativeIndent {
        writeLine("$routesParam.register(\"${endpointAnnotation.path}\", { $rulesField ->")
        withRelativeIndent(4) {
            writeRulesLambda()
        }
        writeLine("})")
    }

    private fun writeRulesLambda() = w.withRelativeIndent {
        for (function in endpointClazz.getDeclaredFunctions()) {
            if (function.isConstructor()) continue
            if (!function.isPublic()) continue
            writeRule(function)
        }
    }

    private val reqField = "req"
    private val resField = "res"

    private fun writeRule(function: KSFunctionDeclaration) = w.withRelativeIndent {
        val lambda = captureRuleLambda(function)
        val route = RouteData.from(function)
        writeLine("$rulesField.${route.methodName}(\"${route.path}\", {$reqField, $resField ->")
        w.write(lambda)
        writeLine("})")
    }

    private fun captureRuleLambda(function: KSFunctionDeclaration) = Utils.capturing { iw ->
        iw.setIndent(w.indent + 4)
        RuleLambdaGenerator(
            function,
            endpointParam,
            reqField,
            resField,
            imports,
            params,
            variables,
            endpointCatalog,
            iw,
        ).write()
    }
}

