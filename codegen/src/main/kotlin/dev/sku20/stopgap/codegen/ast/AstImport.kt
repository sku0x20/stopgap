package dev.sku20.stopgap.codegen.ast

data class AstImport(
    val type: AstType,
    val alias: AstExpression? = null,
) : AstExpression