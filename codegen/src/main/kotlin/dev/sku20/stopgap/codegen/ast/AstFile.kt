package dev.sku20.stopgap.codegen.ast

data class AstFile(
    val packageName: AstExpression,
    val content: List<AstExpression>,
) : AstExpression
