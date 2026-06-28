package dev.sku20.stopgap.app.generator.uuid

import java.util.*

class UuidGen {
    fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
