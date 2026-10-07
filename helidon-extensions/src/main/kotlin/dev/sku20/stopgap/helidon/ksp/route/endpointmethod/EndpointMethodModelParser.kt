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
import dev.sku20.stopgap.helidon.ksp.qualifiedName
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
            httpMethods[annotation.qualifiedName()]!!,
            annotation.argument("path"),
            CustomSerdeCatalogModelParser(function).parse(),
            params.filterIsInstance<AuthParamModel>().firstOrNull(),
            params,
            isResponseUnit(),
        )
    }

    private val httpMethods: Map<String, HttpMethod> = mapOf(
        Get::class.qualifiedName!! to HttpMethod.GET,
        Post::class.qualifiedName!! to HttpMethod.POST,
        Put::class.qualifiedName!! to HttpMethod.PUT,
        Patch::class.qualifiedName!! to HttpMethod.PATCH,
        Delete::class.qualifiedName!! to HttpMethod.DELETE,
        Head::class.qualifiedName!! to HttpMethod.HEAD,
        Options::class.qualifiedName!! to HttpMethod.OPTIONS,
        Trace::class.qualifiedName!! to HttpMethod.TRACE,
    )

    private fun findHttpMethodAnnotation(): KSAnnotation =
        function.annotations.firstOrNull { it.qualifiedName() in httpMethods }
            ?: throw IllegalArgumentException("No Http Method annotation found on function: ${function.qualifiedName!!.asString()}")

    private fun isResponseUnit(): Boolean =
        function.returnType!!.resolve().declaration.qualifiedName!!.asString() == Unit::class.qualifiedName!!
}
