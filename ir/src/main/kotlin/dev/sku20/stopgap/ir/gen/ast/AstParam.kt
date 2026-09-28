package dev.sku20.stopgap.ir.gen.ast

data class AstParam(
    val name: String,
    val type: AstType,
    val annotations: List<AstAnnotation>
)
