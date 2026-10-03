package dev.sku20.stopgap.codegen.ast

interface AstVisitor {
    fun visitAssignment(node: AstAssignment)
    fun visitCall(node: AstCall)
    fun visitFile(node: AstFile)
    fun visitFunction(node: AstFunction)
    fun visitLambda(node: AstLambda)
    fun visitLiteral(node: AstLiteral)
    fun visitParam(node: AstParam)
    fun visitReturn(node: AstReturn)
    fun visitStringLiteral(node: AstStringLiteral)
    fun visitType(node: AstType)
    fun visitWhen(node: AstWhen)
    fun visitWhenBranch(node: AstWhenBranch)
}

fun AstVisitor.visit(node: AstExpression) = when (node) {
    is AstAssignment -> visitAssignment(node)
    is AstCall -> visitCall(node)
    is AstFile -> visitFile(node)
    is AstFunction -> visitFunction(node)
    is AstLambda -> visitLambda(node)
    is AstLiteral -> visitLiteral(node)
    is AstParam -> visitParam(node)
    is AstReturn -> visitReturn(node)
    is AstStringLiteral -> visitStringLiteral(node)
    is AstType -> visitType(node)
    is AstWhen -> visitWhen(node)
    is AstWhenBranch -> visitWhenBranch(node)
}
