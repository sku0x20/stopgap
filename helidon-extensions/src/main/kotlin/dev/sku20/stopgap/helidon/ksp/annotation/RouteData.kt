package dev.sku20.stopgap.helidon.ksp.annotation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.stopgap.helidon.endpoint.*
import dev.sku20.stopgap.helidon.ksp.argument

class RouteData(val methodName: String, val path: String) {
    companion object {
        fun from(function: KSFunctionDeclaration): RouteData {
            for (annotation in function.annotations) {
                val methodName = httpMethodFnName(annotation) ?: continue
                return RouteData(methodName, annotation.argument("path"))
            }
            throw IllegalArgumentException("No Http Method annotation found on function: ${function.simpleName.asString()}")
        }

        // internal mapping, decoupled from client annotations - switch is fine
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
    }
}
