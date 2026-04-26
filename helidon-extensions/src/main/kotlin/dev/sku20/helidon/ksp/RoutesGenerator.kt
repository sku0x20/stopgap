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

    private val endpointField = "endpoint"
    private val routesField = "routes"

    private fun writeFunctionDefinition() = w.withRelativeIndent {
        val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
        writeLine("fun registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$endpointField: ${endpointQualifiedName},")
            writeLine("$routesField: HttpRouting.Builder,")
            writeLine("serde: Serde = NopSerde,")
        }
        writeLine("){")
    }

    private val rulesField = "rules"

    private fun writeFunctionBody() = w.withRelativeIndent {
        val endpointPath = pathValue(endpointAnnotation)
        writeLine("$routesField.register(\"${endpointPath}\", { $rulesField ->")
        withRelativeIndent(4) {
            writeRules()
        }
        writeLine("})")
    }

    private fun writeRules() = w.withRelativeIndent {
        writeLine(rulesField)
        withRelativeIndent(4) {
            for (function in endpointClazz.getDeclaredFunctions()) {
                if (!function.isPublic()) continue
                writeRoute(function)
            }
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
        writeLine(".${functionName}(\"${pathValue(annotation)}\", {req, res ->")
        withRelativeIndent(4) {
            writeHandler(endpointFunction)
        }
        writeLine("})")
    }

    fun writeHandler(function: KSFunctionDeclaration) =
        HandlerGenerator(function, endpointField, w)
            .write()

    private fun pathValue(annotation: KSAnnotation): String {
        val path = annotation.arguments.first { it.name?.getShortName() == "path" }
        val pathValue = path.value as String
        return pathValue
    }

    private fun findEndpointAnnotationOnClazz(): KSAnnotation =
        endpointClazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
}
