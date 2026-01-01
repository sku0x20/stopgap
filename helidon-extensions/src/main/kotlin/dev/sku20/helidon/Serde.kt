package dev.sku20.helidon

interface Serde {
    fun serialize(obj: Any): String
    fun deserialize(str: String): Any
}