package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.*
import java.io.OutputStream

class AstPrinter(
    private val out: OutputStream,
) : AstVisitor {

    override fun visitAssignment(node: AstAssignment) {
        TODO("Not yet implemented")
    }

    override fun visitCall(node: AstCall) {
        TODO("Not yet implemented")
    }

    override fun visitFile(node: AstFile) {
        write("package ")
        visit(node.packageName)
        writeln()
        node.content.forEach {
            writeln()
            visit(it)
            writeln()
        }
    }

    override fun visitFunction(node: AstFunction) {
        TODO("Not yet implemented")
    }

    override fun visitLambda(node: AstLambda) {
        TODO("Not yet implemented")
    }

    override fun visitLiteral(node: AstLiteral) {
        write(node.value)
    }

    override fun visitParam(node: AstParam) {
        TODO("Not yet implemented")
    }

    override fun visitReturn(node: AstReturn) {
        TODO("Not yet implemented")
    }

    override fun visitStringLiteral(node: AstStringLiteral) {
        write("\"")
        write(node.value)
        write("\"")
    }

    override fun visitType(node: AstType) {
        TODO("Not yet implemented")
    }

    private fun write(text: String) {
        out.write(text.toByteArray())
    }

    private fun writeln() {
        write("\n")
    }

}
