package dev.sku20.stopgap.helidon.ksp.route.endpointmethod

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.stopgap.helidon.endpoint.Delete
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.endpoint.Head
import dev.sku20.stopgap.helidon.endpoint.Options
import dev.sku20.stopgap.helidon.endpoint.Patch
import dev.sku20.stopgap.helidon.endpoint.Post
import dev.sku20.stopgap.helidon.endpoint.Put
import dev.sku20.stopgap.helidon.endpoint.Trace
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.route.customserdecatalog.CustomSerdeCatalogModelParser
import dev.sku20.stopgap.helidon.ksp.route.param.AuthParamModel
import dev.sku20.stopgap.helidon.ksp.route.param.ParamModelParser

class EndpointMethodModelParser(private val function: KSFunctionDeclaration) {

    private val name = function.simpleName.asString()

    fun parse(): EndpointMethodModel {
        val annotation = findHttpMethodAnnotation()
        val params = function.parameters.map { ParamModelParser(it).parse() }
        return EndpointMethodModel(
            name,
            httpMethods[annotation.shortName.asString()]!!,
            annotation.argument("path"),
            CustomSerdeCatalogModelParser(function).parse(),
            params.filterIsInstance<AuthParamModel>().firstOrNull(),
            params,
            isResponseUnit(),
        )
    }

    private val httpMethods: Map<String, HttpMethod> = mapOf(
        Get::class.simpleName!! to HttpMethod.GET,
        Post::class.simpleName!! to HttpMethod.POST,
        Put::class.simpleName!! to HttpMethod.PUT,
        Patch::class.simpleName!! to HttpMethod.PATCH,
        Delete::class.simpleName!! to HttpMethod.DELETE,
        Head::class.simpleName!! to HttpMethod.HEAD,
        Options::class.simpleName!! to HttpMethod.OPTIONS,
        Trace::class.simpleName!! to HttpMethod.TRACE,
    )

    private fun findHttpMethodAnnotation(): KSAnnotation =
        function.annotations.firstOrNull { it.shortName.asString() in httpMethods }
            ?: throw IllegalArgumentException("No Http Method annotation found on function: ${function.qualifiedName!!.asString()}")

    private fun isResponseUnit(): Boolean =
        function.returnType!!.resolve().declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!
}
