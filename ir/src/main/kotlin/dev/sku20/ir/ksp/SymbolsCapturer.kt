package dev.sku20.ir.ksp

class SymbolsCapturer<T> {

    private val symbols = mutableListOf<T>()

    fun capture(list: List<T>) {
        symbols.addAll(list)
    }

    fun getSymbols(): List<T> {
        return symbols
    }

}