package dev.sku20.stopgap.codegen.ast

data class AstWhen(
    val branches: List<AstWhenBranch>,
    val subject: AstExpression? = null,
    val elseContent: List<AstExpression> = emptyList(),
) : AstExpression

data class AstWhenBranch(
    val conditions: List<AstExpression>,
    val content: List<AstExpression>,
) : AstExpression
