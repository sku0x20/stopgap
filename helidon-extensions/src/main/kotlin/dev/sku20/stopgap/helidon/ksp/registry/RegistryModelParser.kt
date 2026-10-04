package dev.sku20.stopgap.helidon.ksp.registry

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.stopgap.helidon.ksp.annotation.CustomSerdeCatalogData

class RegistryModelParser(private val functions: List<KSFunctionDeclaration>) {

    fun parse(): RegistryModel {
        val endpoints = mutableListOf<RegistryModel.Endpoint>()
        for (function in functions) {
            endpoints.add(parseEndpoint(function))
        }
        return RegistryModel(endpoints)
    }

    private fun parseEndpoint(function: KSFunctionDeclaration): RegistryModel.Endpoint {
        val catalogs = mutableListOf<RegistryModel.Catalog>()
        for (i in 2 until function.parameters.size) {
            catalogs.add(parseCatalog(function.parameters[i]))
        }
        return RegistryModel.Endpoint(qualifiedTypeName(function.parameters.first()), catalogs)
    }

    private fun parseCatalog(param: KSValueParameter): RegistryModel.Catalog {
        val catalog = CustomSerdeCatalogData.from(param)
        if (!catalog.qualifier.isNullOrEmpty()) {
            return RegistryModel.Catalog(null, catalog.qualifier)
        }
        return RegistryModel.Catalog(catalog.clazz!!.declaration.qualifiedName!!.asString(), null)
    }

    private fun qualifiedTypeName(param: KSValueParameter): String =
        param.type.resolve().declaration.qualifiedName!!.asString()
}
