package dev.sku20.stopgap.ir.gen.ast

data class AstFunction (
    val name: String,
    val visibility: AstVisibility,
    val params: List<AstParam>,
) : AstElement
