package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.endpoint.*
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils

class RoutesBodyGenerator(
    private val endpointClazz: KSClassDeclaration,
    private val endpointParam: String,
    private val routesParam: String,
    private val w: CustomWriter,
    private val imports: MutableSet<String>,
    private val params: MutableSet<String>,
) {
    private val endpointAnnotation = findEndpointAnnotationOnClazz()
    private val rulesField = "rules"

    fun write() = w.withRelativeIndent {
        val endpointPath = pathValue(endpointAnnotation)
        writeLine("$routesParam.register(\"${endpointPath}\", { $rulesField ->")
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

    private val ruleVariables = mutableSetOf<String>()
    private fun writeRule(function: KSFunctionDeclaration) = w.withRelativeIndent {
        val lambda = captureRuleLambda(function)
        for (variable in ruleVariables) writeLine("val $variable")
        val (methodFn, path) = httpMethodFor(function)
        writeLine("$rulesField.${methodFn}(\"${path}\", {$reqField, $resField ->")
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
            iw,
            imports,
            params,
            ruleVariables,
        ).write()
    }

    private fun httpMethodFor(function: KSFunctionDeclaration): Pair<String, String> {
        for (annotation in function.annotations) {
            val methodFn = httpMethodFnName(annotation) ?: continue
            return Pair(methodFn, pathValue(annotation))
        }
        throw IllegalArgumentException("No Http Method annotation found on function: ${function.simpleName.asString()}")
    }

    /**
     * fine with switch here.
     * - internal impl; decoupled from client annotations
     * - flexible; enable easy change later if required
     * - mapping needs to be somewhere, either
     *  - raw switch;
     *  - hashmap lookup;
     *  - or some kind of indirection, via factory or other techniques.
     */
    // @formatter:off
    private fun httpMethodFnName(annotation: KSAnnotation) = when (annotation.shortName.asString()) {
        Get::class.simpleName -> "get"
        Post::class.simpleName -> "post"
        Delete::class.simpleName -> "delete"
        Put::class.simpleName -> "put"
        Patch::class.simpleName -> "patch"
        Head::class.simpleName -> "head"
        Options::class.simpleName -> "options"
        Trace::class.simpleName -> "trace"
        else -> null
    }
    // @formatter:on

    private fun pathValue(annotation: KSAnnotation): String {
        val path = annotation.arguments.first { it.name?.getShortName() == "path" }
        return path.value as String
    }

    private fun findEndpointAnnotationOnClazz(): KSAnnotation =
        endpointClazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
}
