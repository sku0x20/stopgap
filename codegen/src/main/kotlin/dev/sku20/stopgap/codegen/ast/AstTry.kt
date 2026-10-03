package dev.sku20.stopgap.codegen.ast

data class AstTry(
    val content: List<AstExpression>,
    val astCatch: List<AstCatch> = emptyList(),
    val finallyContent: List<AstExpression> = emptyList(),
) : AstExpression

data class AstCatch(
    val parameter: AstExpression,
    val content: List<AstExpression>,
) : AstExpression
