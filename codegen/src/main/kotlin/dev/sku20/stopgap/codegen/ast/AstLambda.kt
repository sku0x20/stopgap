package dev.sku20.stopgap.codegen.ast

data class AstLambda(
    val parameters: List<AstParam>,
    val content: List<AstExpression>,
) : AstExpression
