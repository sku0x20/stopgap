package dev.sku20.stopgap.ir.ksp.creator

data class CreatorModel(
    val functionName: String,
    val returnType: String,
    val qualifier: String?,
    val eagerly: Boolean,
    val parameters: List<Parameter>
) {
    data class Parameter(
        val type: String,
        val qualifier: String?
    )
}
