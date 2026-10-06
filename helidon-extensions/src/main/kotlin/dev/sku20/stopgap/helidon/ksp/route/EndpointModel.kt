package dev.sku20.stopgap.helidon.ksp.route

import dev.sku20.stopgap.helidon.ksp.route.customserdecatalog.CustomSerdeCatalogModel
import dev.sku20.stopgap.helidon.ksp.route.endpointmethod.EndpointMethodModel

data class EndpointModel(
    val fqn: String,
    val path: String,
    val methods: List<EndpointMethodModel>,
    val customSerdeCatalogModel: CustomSerdeCatalogModel?,
)
