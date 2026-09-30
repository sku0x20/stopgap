package dev.sku20.stopgap.codegen.ast

data class AstAssignment(
    val name: AstExpression,
    val type: VariableType,
    val value: AstExpression,
) : AstExpression

@Suppress("SpellCheckingInspection")
enum class VariableType {
    VAL,
    VAR,
    LATEINIT_VAR,
    EMPTY,
}
