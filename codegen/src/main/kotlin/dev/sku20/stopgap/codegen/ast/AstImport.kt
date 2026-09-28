package dev.sku20.stopgap.codegen.ast

data class AstImport(
    val fqn: String,
    val alias: String?,
) : AstElement
