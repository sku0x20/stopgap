package dev.sku20.stopgap.ir.gen.ast

data class KParam(
    val name: String,
    val type: KType,
    val annotations: List<KAnnotation>
)
