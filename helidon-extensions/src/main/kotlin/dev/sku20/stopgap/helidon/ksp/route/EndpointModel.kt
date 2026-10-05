package dev.sku20.stopgap.helidon.ksp.route

data class EndpointModel(
    val fqn: String,
    val path: String,
    val methods: List<EndpointMethodModel>,
)
