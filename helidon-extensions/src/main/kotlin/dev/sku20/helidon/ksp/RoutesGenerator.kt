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
    private val endpointInstance = "endpoint"

    fun write() = w.withRelativeIndent {
        val endpointAnnotation =
            endpointClazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
        val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$endpointInstance: ${endpointQualifiedName},")
            writeLine("routes: HttpRouting.Builder,")
            writeLine("serde: Serde = NopSerde,")
        }
        writeLine("){")
        withRelativeIndent(4) {
            registerEndpoint(path(endpointAnnotation))
        }
        writeLine("}")
    }

    @Suppress("SameParameterValue")
    private fun registerEndpoint(endpointPath: String) = w.withRelativeIndent {
        writeLine("routes.register(\"${endpointPath}\", { rules ->")
        withRelativeIndent(4) {
            writeLine("rules")
            withRelativeIndent(4) {
                writeRulesFromFunctions()
            }
        }
        writeLine("})")
    }

    private fun writeRulesFromFunctions() = w.withRelativeIndent {
        for (function in endpointClazz.getDeclaredFunctions()) {
            if (!function.isPublic()) continue
            writeRoute(function)
        }
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
    fun writeRoute(function: KSFunctionDeclaration) {
        for (annotation in function.annotations) {
            when (annotation.shortName.asString()) {
                Get::class.simpleName -> writeViaFunctionName("get", annotation, function)
                Post::class.simpleName -> writeViaFunctionName("post", annotation, function)
                Delete::class.simpleName -> writeViaFunctionName("delete", annotation, function)
                Put::class.simpleName -> writeViaFunctionName("put", annotation, function)
                Patch::class.simpleName -> writeViaFunctionName("patch", annotation, function)
                Head::class.simpleName -> writeViaFunctionName("head", annotation, function)
                Options::class.simpleName -> writeViaFunctionName("options", annotation, function)
                Trace::class.simpleName -> writeViaFunctionName("trace", annotation, function)
            }
        }
    }
    // @formatter:on

    fun writeViaFunctionName(
        functionName: String,
        annotation: KSAnnotation,
        endpointFunction: KSFunctionDeclaration,
    ) = w.withRelativeIndent {
        writeLine(".${functionName}(\"${path(annotation)}\", {req, res ->")
        withRelativeIndent(4) {
            writeHandler(endpointFunction)
        }
        writeLine("})")
    }

    fun writeHandler(function: KSFunctionDeclaration) =
        HandlerGenerator(function, endpointInstance, w)
            .write()

    private fun path(annotation: KSAnnotation): String {
        val path = annotation.arguments.first { it.name?.getShortName() == "path" }
        val pathValue = path.value as String
        return pathValue
    }
}
