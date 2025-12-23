package dev.sku20.ir.ksp

import java.io.OutputStream

class InitCreatorsGen(
    packageName: String,
    private val fileName: String,
    outputStream: OutputStream
) {

    private val writer = outputStream.bufferedWriter()

    init {
        writer.write("package $packageName\n")
    }

    fun finish() {
        writer.close()
    }

}