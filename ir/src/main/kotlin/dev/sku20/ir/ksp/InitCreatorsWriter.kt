package dev.sku20.ir.ksp

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.OutputStream

class InitCreatorsWriter(
    file: OutputStream,
    private val symbols: List<KSFunctionDeclaration>,
    private val packageName: String,
    private val fileName: String
) {
    private val writer = file.bufferedWriter()

    fun write() {
        writer.appendLine("package $packageName")
        writer.close()
    }

}
