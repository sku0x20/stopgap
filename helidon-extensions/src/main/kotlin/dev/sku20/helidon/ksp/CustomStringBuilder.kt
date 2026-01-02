package dev.sku20.helidon.ksp

class CustomStringBuilder {

    private val sb = StringBuilder()

    fun writeLine(s: String = "") {
        writeIndent()
        sb.append(s)
        sb.append("\n")
    }

    override fun toString(): String = sb.toString()

    fun withRelativeIndent(n: Int = 0, block: CustomStringBuilder.() -> Unit) {
        increaseIndentBy(n)
        block()
        decreaseIndentBy(n)
    }

    fun increaseIndentBy(n: Int) {
        indent += n
    }

    fun decreaseIndentBy(n: Int) {
        indent -= n
    }

    fun setIndent(n: Int) {
        indent = n
    }

    private var indent = 0

    @Suppress("EmptyRange")
    private fun writeIndent() {
        for (i in 0 until indent) {
            sb.append(" ")
        }
    }

}