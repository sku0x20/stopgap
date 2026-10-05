package dev.sku20.stopgap.helidon.ksp.registry

data class RegistryModel(
    val endpoints: List<Endpoint>
) {
    data class Endpoint(
        val type: String,
        val params: List<Param>
    )

    data class Param(
        val type: String,
        val qualifier: String?
    )
}
