package dev.sku20.stopgap.helidon.ksp.registry

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.findAnnotation

class RegistryModelParser(private val functions: List<KSFunctionDeclaration>) {

    fun parse(): RegistryModel {
        val endpoints = mutableListOf<RegistryModel.Endpoint>()
        for (function in functions) {
            endpoints.add(parseEndpoint(function))
        }
        return RegistryModel(endpoints)
    }

    private fun parseEndpoint(function: KSFunctionDeclaration): RegistryModel.Endpoint {
        val params = mutableListOf<RegistryModel.Param>()
        for (i in 2 until function.parameters.size) {
            params.add(parseParam(function.parameters[i]))
        }
        return RegistryModel.Endpoint(qualifiedTypeName(function.parameters.first()), params)
    }

    private fun parseParam(param: KSValueParameter): RegistryModel.Param {
        val qualifier = param.findAnnotation(RegistryQualifier::class)?.argument<String>("value")
        return RegistryModel.Param(qualifiedTypeName(param), qualifier)
    }

    private fun qualifiedTypeName(param: KSValueParameter): String =
        param.type.resolve().declaration.qualifiedName!!.asString()
}
