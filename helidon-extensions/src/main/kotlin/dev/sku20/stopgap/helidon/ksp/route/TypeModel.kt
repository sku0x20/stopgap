package dev.sku20.stopgap.helidon.ksp.route

data class TypeModel(
    val fqn: String,
    val arguments: List<TypeModel> = emptyList(),
    val isNullable: Boolean = false,
)
