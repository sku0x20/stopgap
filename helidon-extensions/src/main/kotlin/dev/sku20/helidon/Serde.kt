package dev.sku20.helidon

interface Serde {
    fun <T> serialize(obj: T): String
    fun <T> deserialize(str: String): T
}