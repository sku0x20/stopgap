package dev.sku20.stopgap.helidon.ksp.route

data class EndpointMethodModel(
    val name: String,
    val type: String,
    val path: String,
    val customSerdeCatalog: CustomSerdeCatalogModel,
    val auth: Auth,
    // in declaration order
    val params: List<Param> = emptyList(),
    val responseFqn: String? = null,
)
