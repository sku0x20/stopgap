package dev.sku20.stopgap.helidon.ksp.route

data class EndpointMethodModel(
    val name: String,
    val type: String,
    val path: String,
    val customSerdeCatalog: CustomSerdeCatalogModel? = null,
    val req: Boolean = false,
    val rest: Boolean = false,
    val authentication: Param? = null,
    val body: Param? = null,
    val responseFqn: String? = null,
    val headers: List<HeaderParam> = emptyList(),
    val queryParams: List<QueryParam> = emptyList(),
    val pathParams: List<PathParam> = emptyList(),
)
