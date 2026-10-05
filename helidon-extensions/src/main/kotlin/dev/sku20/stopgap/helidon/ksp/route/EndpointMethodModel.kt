package dev.sku20.stopgap.helidon.ksp.route

import dev.sku20.stopgap.helidon.ksp.route.auth.AuthModel
import dev.sku20.stopgap.helidon.ksp.route.param.ParamModel

data class EndpointMethodModel(
    val name: String,
    val httpMethod: HttpMethod,
    val path: String,
    val customSerdeCatalog: CustomSerdeCatalogModel,
    val auth: AuthModel,
    // in declaration order
    val params: List<ParamModel> = emptyList(),
    val response: String? = null,
)
