package dev.sku20.stopgap.codegen.ast

data class AstAssignment(
    val name: AstExpression,
    val type: AstExpression? = null,
    val variableType: VariableType? = null,
    val value: AstExpression? = null,
    val visibility: Visibility? = null,
) : AstExpression

@Suppress("SpellCheckingInspection")
enum class VariableType {
    VAL,
    VAR,
    LATEINIT_VAR,
}
