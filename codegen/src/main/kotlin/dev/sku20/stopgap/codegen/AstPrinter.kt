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
        if (node.receiver != null) {
            visit(node.receiver)
            write(".")
        }
        visit(node.name)
        write("(")
        if (node.arguments.isNotEmpty()) {
            visit(node.arguments[0])
        }
        for (index in 1 until node.arguments.size) {
            write(", ")
            visit(node.arguments[index])
        }
        write(")")
    }

    override fun visitFile(node: AstFile) {
        write("package ")
        visit(node.packageName)
        writeln()
        for (item in node.content) {
            writeln()
            visit(item)
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
        visit(node.name)
        if (node.type != null) {
            write(": ")
            visit(node.type)
        }
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
        write(node.fqn)
    }

    private fun write(text: String) {
        out.write(text.toByteArray())
    }

    private fun writeln() {
        write("\n")
    }

}
