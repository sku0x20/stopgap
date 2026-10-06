package dev.sku20.stopgap.helidon.ksp.route

import dev.sku20.stopgap.helidon.ksp.customserdecatalog.CustomSerdeCatalogModel

data class EndpointMethodModel(
    val name: String,
    val httpMethod: HttpMethod,
    val path: String,
    val customSerdeCatalogModel: CustomSerdeCatalogModel? = null,
    // its a param but to save lookup since it has to be on each method.
    val authParamModel: AuthParamModel? = null,
    val params: List<ParamModel> = emptyList(),
    val isResponseUnit: Boolean = true,
)
