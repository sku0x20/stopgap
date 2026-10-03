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
}

fun AstExpression.accept(visitor: AstVisitor) = when (this) {
    is AstAssignment -> visitor.visitAssignment(this)
    is AstCall -> visitor.visitCall(this)
    is AstFile -> visitor.visitFile(this)
    is AstFunction -> visitor.visitFunction(this)
    is AstLambda -> visitor.visitLambda(this)
    is AstLiteral -> visitor.visitLiteral(this)
    is AstParam -> visitor.visitParam(this)
    is AstReturn -> visitor.visitReturn(this)
    is AstStringLiteral -> visitor.visitStringLiteral(this)
    is AstType -> visitor.visitType(this)
}
