package dev.sku20.stopgap.codegen.ast

data class AstVariable(
    val type: VariableType,
    val name: String,
    val expression: AstExpression? = null,
)

enum class VariableType {
    VAL,
    VAR,
}

// function a(){
// val a = xyz;
// a = 100;
// return 10;
//}