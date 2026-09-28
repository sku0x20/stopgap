package dev.sku20.stopgap.codegen.ast

sealed interface AstElement

fun AstElement.accept(visitor: AstVisitor) = when (this) {
    is AstFile -> visitor.visitFile(this)
    is AstImport -> visitor.visitImport(this)
    is AstFunction -> visitor.visitFunction(this)
    is AstParam -> visitor.visitParam(this)
    is AstType -> visitor.visitType(this)
    is AstAnnotation -> visitor.visitAnnotation(this)
    is AstReturn -> visitor.visitReturn(this)
    is AstCall -> visitor.visitCall(this)
    is AstLiteral -> visitor.visitLiteral(this)
    is AstReference -> visitor.visitReference(this)
}
