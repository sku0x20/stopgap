package dev.sku20.stopgap.helidon.ksp.route

import dev.sku20.stopgap.helidon.ksp.customserdecatalog.CustomSerdeCatalogModel

data class EndpointModel(
    val fqn: String,
    val path: String,
    val methods: List<EndpointMethodModel>,
    val customSerdeCatalogModel: CustomSerdeCatalogModel?,
)
