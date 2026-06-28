package dev.sku20.stopgap.helidon.ksp.endpoint

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.stopgap.helidon.ksp.CustomWriter
import dev.sku20.stopgap.helidon.ksp.Utils
import dev.sku20.stopgap.helidon.ksp.annotation.CustomSerdeCatalogData
import dev.sku20.stopgap.helidon.ksp.annotation.EndpointData
import dev.sku20.stopgap.helidon.ksp.annotation.RouteData

class RoutesBodyGenerator(
    private val endpointClazz: KSClassDeclaration,
    private val imports: MutableSet<String>,
    private val params: MutableSet<String>,
    private val variables: MutableSet<String>,
    private val w: CustomWriter,
) {
    private val endpointData = EndpointData.from(endpointClazz)
    private val endpointCatalog = CustomSerdeCatalogData.from(endpointClazz)

    fun write() = w.withRelativeIndent {
        writeLine("${GeneratedNames.ROUTES}.register(\"${endpointData.path}\", { ${GeneratedNames.RULES} ->")
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

    private fun writeRule(function: KSFunctionDeclaration) = w.withRelativeIndent {
        val lambda = captureRuleLambda(function)
        val route = RouteData.from(function)
        writeLine("${GeneratedNames.RULES}.${route.methodName}(\"${route.path}\", {${GeneratedNames.REQ}, ${GeneratedNames.RES} ->")
        w.write(lambda)
        writeLine("})")
    }

    private fun captureRuleLambda(function: KSFunctionDeclaration) = Utils.capturing { iw ->
        iw.setIndent(w.indent + 4)
        RuleLambdaGenerator(
            function,
            imports,
            params,
            variables,
            endpointCatalog,
            iw,
        ).write()
    }
}

