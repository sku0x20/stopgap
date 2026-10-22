package dev.sku20.stopgap.app.serde

// Deliberately not @JvmRecord: checks fastjson2 resolves constructor params without kotlin-reflect.
data class DataClassReq(
    val id: Int,
    val name: String
)
