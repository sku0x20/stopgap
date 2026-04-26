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
    fun write() = w.withRelativeIndent {
        val endpointAnnotation =
            endpointClazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
        val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
        val endpointInstance = "endpoint"
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$endpointInstance: ${endpointQualifiedName},")
            writeLine("routes: HttpRouting.Builder,")
            writeLine("serde: Serde = NopSerde,")
        }
        writeLine("){")
        withRelativeIndent(4) {
            registerEndpoint(
                path(endpointAnnotation),
                endpointInstance,
                endpointClazz.getDeclaredFunctions()
            )
        }
        writeLine("}")
    }

    @Suppress("SameParameterValue")
    private fun registerEndpoint(
        endpointPath: String,
        endpointInstance: String,
        functions: Sequence<KSFunctionDeclaration>
    ) = w.withRelativeIndent {
        writeLine("routes.register(\"${endpointPath}\", { rules ->")
        withRelativeIndent(4) {
            writeLine("rules")
            withRelativeIndent(4) {
                writeRulesFromFunctions(endpointInstance, functions)
            }
        }
        writeLine("})")
    }

    private fun writeRulesFromFunctions(
        endpointInstance: String,
        functions: Sequence<KSFunctionDeclaration>
    ) = w.withRelativeIndent {
        for (function in functions) {
            if (!function.isPublic()) continue
            writeRoute(function, endpointInstance)
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
    fun writeRoute(
        function: KSFunctionDeclaration,
        endpointInstance: String
    ) {
        for (annotation in function.annotations) {
            when (annotation.shortName.asString()) {
                Get::class.simpleName -> writeViaFunctionName("get", annotation, function, endpointInstance)
                Post::class.simpleName -> writeViaFunctionName("post", annotation, function, endpointInstance)
                Delete::class.simpleName -> writeViaFunctionName("delete", annotation, function, endpointInstance)
                Put::class.simpleName -> writeViaFunctionName("put", annotation, function, endpointInstance)
                Patch::class.simpleName -> writeViaFunctionName("patch", annotation, function, endpointInstance)
                Head::class.simpleName -> writeViaFunctionName("head", annotation, function, endpointInstance)
                Options::class.simpleName -> writeViaFunctionName("options", annotation, function, endpointInstance)
                Trace::class.simpleName -> writeViaFunctionName("trace", annotation, function, endpointInstance)
            }
        }
    }
    // @formatter:on

    fun writeViaFunctionName(
        functionName: String,
        annotation: KSAnnotation,
        endpointFunction: KSFunctionDeclaration,
        endpointInstance: String
    ) = w.withRelativeIndent {
        writeLine(".${functionName}(\"${path(annotation)}\", {req, res ->")
        withRelativeIndent(4) {
            writeHandler(endpointFunction, endpointInstance)
        }
        writeLine("})")
    }

    fun writeHandler(
        function: KSFunctionDeclaration,
        endpointInstance: String
    ) = HandlerGenerator(function, endpointInstance, w)
        .write()

    private fun path(annotation: KSAnnotation): String {
        val path = annotation.arguments.first { it.name?.getShortName() == "path" }
        val pathValue = path.value as String
        return pathValue
    }
}
