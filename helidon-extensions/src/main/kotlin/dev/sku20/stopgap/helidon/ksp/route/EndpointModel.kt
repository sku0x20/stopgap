package dev.sku20.stopgap.helidon.ksp.route


data class EndpointModel(
    val fqn: String,
    val path: String,
    val serdeCatalog: CustomSerdeCatalogModel,
    val methods: List<EndpointMethodModel>,
)
