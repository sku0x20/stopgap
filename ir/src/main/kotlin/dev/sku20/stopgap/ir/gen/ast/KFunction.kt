package dev.sku20.stopgap.ir.gen.ast

data class KFunction (
    val name: String,
    val visibility: KVisibility,
    val params: List<KParam>,
)
