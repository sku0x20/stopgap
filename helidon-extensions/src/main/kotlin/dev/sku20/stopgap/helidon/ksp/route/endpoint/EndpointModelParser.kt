package dev.sku20.stopgap.helidon.ksp.route.endpoint

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.findAnnotation
import dev.sku20.stopgap.helidon.ksp.route.customserdecatalog.CustomSerdeCatalogModelParser
import dev.sku20.stopgap.helidon.ksp.route.endpointmethod.EndpointMethodModel
import dev.sku20.stopgap.helidon.ksp.route.endpointmethod.EndpointMethodModelParser

class EndpointModelParser(private val clazz: KSClassDeclaration) {

    private val fqn = clazz.qualifiedName!!.asString()

    fun parse(): EndpointModel {
        val annotation = clazz.findAnnotation(Endpoint::class)
            ?: throw IllegalArgumentException("No Endpoint annotation found on class: $fqn")
        return EndpointModel(
            fqn,
            annotation.argument("path"),
            parseMethods(),
            CustomSerdeCatalogModelParser(clazz).parse(),
        )
    }

    private fun parseMethods(): List<EndpointMethodModel> {
        val methods = mutableListOf<EndpointMethodModel>()
        for (function in clazz.getDeclaredFunctions()) {
            if (function.isConstructor()) continue
            if (!function.isPublic()) continue
            methods.add(EndpointMethodModelParser(function).parse())
        }
        return methods
    }
}
