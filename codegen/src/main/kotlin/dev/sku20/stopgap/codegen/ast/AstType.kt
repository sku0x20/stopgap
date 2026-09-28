package dev.sku20.stopgap.codegen.ast

data class AstType(
    val shortName: String,
    val fqn: String,
) : AstElement
