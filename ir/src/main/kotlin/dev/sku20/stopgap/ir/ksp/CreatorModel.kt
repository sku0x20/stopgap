package dev.sku20.stopgap.ir.ksp

internal data class CreatorModel(
    val functionName: String,
    val returnType: String,
    val qualifier: String?,
    val eagerly: Boolean,
    val dependencies: List<Dependency>
) {
    data class Dependency(
        val type: String,
        val qualifier: String?
    )
}
