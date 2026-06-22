package dev.sku20.helidon.ksp.endpoint.annotation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.endpoint.Delete
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.endpoint.Head
import dev.sku20.helidon.endpoint.Options
import dev.sku20.helidon.endpoint.Patch
import dev.sku20.helidon.endpoint.Post
import dev.sku20.helidon.endpoint.Put
import dev.sku20.helidon.endpoint.Trace
import dev.sku20.helidon.ksp.argument

class RouteData(val methodName: String, val path: String) {
    companion object {
        fun from(function: KSFunctionDeclaration): RouteData {
            for (annotation in function.annotations) {
                val methodName = httpMethodFnName(annotation) ?: continue
                return RouteData(methodName, annotation.argument("path"))
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
    }
}
