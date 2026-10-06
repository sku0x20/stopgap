package dev.sku20.stopgap.helidon.ksp.route

data class TypeModel(
    val fqn: String,
    val typeArguments: List<TypeModel> = emptyList(),
    val isNullable: Boolean = false,
)
