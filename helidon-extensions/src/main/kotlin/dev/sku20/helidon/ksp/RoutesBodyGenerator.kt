package dev.sku20.helidon.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.endpoint.*

class RoutesBodyGenerator(
    private val endpointClazz: KSClassDeclaration,
    private val endpointAnnotation: KSAnnotation,
    private val endpoint: String,
    private val routes: String,
    private val defaultSerde: String,
    private val w: CustomWriter
) {
    private val rulesField = "rules"
    private val reqField = "req"
    private val resField = "res"

    fun write() = w.withRelativeIndent {
        val endpointPath = pathValue(endpointAnnotation)
        writeLine("$routes.register(\"${endpointPath}\", { $rulesField ->")
        withRelativeIndent(4) {
            writeRulesLambda()
        }
        writeLine("})")
    }

    private fun writeRulesLambda() = w.withRelativeIndent {
        writeLine(rulesField)
        withRelativeIndent(4) {
            for (function in endpointClazz.getDeclaredFunctions()) {
                if (!function.isPublic()) continue
                writeRule(function)
            }
        }
    }

    private fun writeRule(function: KSFunctionDeclaration) = w.withRelativeIndent {
        val (methodFn, path) = httpMethodFor(function)
        writeLine(".${methodFn}(\"${path}\", {$reqField, $resField ->")
        withRelativeIndent(4) {
            RuleLambdaGenerator(function, endpoint, reqField, resField, defaultSerde, w).write()
        }
        writeLine("})")
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
}
