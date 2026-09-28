package dev.sku20.stopgap.codegen.ast

sealed interface AstElement

fun AstElement.accept(visitor: AstVisitor) = when (this) {
    is AstFunction -> visitor.visitFunction(this)
    is AstParam -> visitor.visitParam(this)
    is AstType -> visitor.visitType(this)
    is AstAnnotation -> visitor.visitAnnotation(this)
}
