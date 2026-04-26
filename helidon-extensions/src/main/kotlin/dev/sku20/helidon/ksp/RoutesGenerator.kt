package dev.sku20.helidon.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.endpoint.*

class RoutesGenerator(
    private val endpointClazz: KSClassDeclaration,
    private val w: CustomWriter
) {
    private val endpointAnnotation = findEndpointAnnotationOnClazz()

    fun write() = w.withRelativeIndent {
        writeFunctionDefinition()
        withRelativeIndent(4) {
            writeFunctionBody()
        }
        writeLine("}")
    }

    private val endpoint = "endpoint"
    private val routes = "routes"

    private fun writeFunctionDefinition() = w.withRelativeIndent {
        val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$endpoint: ${endpointQualifiedName},")
            writeLine("$routes: HttpRouting.Builder,")
            writeLine("serde: Serde = NopSerde,")
        }
        writeLine("){")
    }

    private val rulesField = "rules"

    private fun writeFunctionBody() = w.withRelativeIndent {
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

    private val reqField = "req"
    private val resField = "res"

    private fun writeRule(function: KSFunctionDeclaration) = w.withRelativeIndent {
        val (methodFn, path) = findHttpMethod(function)
        writeLine(".${methodFn}(\"${path}\", {$reqField, $resField ->")
        withRelativeIndent(4) {
            RuleLambdaGenerator(function, endpoint, w).write()
        }
        writeLine("})")
    }

    private fun findHttpMethod(function: KSFunctionDeclaration): Pair<String, String> {
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
    private fun httpMethodFnName(
        annotation: KSAnnotation
    ) = when (annotation.shortName.asString()) {
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

    private fun pathValue(annotation: KSAnnotation): String {
        val path = annotation.arguments.first { it.name?.getShortName() == "path" }
        val pathValue = path.value as String
        return pathValue
    }

    private fun findEndpointAnnotationOnClazz(): KSAnnotation =
        endpointClazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
}
