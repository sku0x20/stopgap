package dev.sku20.helidon.endpoint.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStream

class InitializersGenerator(
    file: OutputStream,
    private val symbols: List<KSClassDeclaration>,
    private val packageName: String
) {
    private val w = CustomWriter(file)

    fun write() {
        w.close()
    }
}