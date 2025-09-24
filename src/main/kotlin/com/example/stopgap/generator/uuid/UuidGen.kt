package com.example.stopgap.generator.uuid

import java.util.*

class UuidGen {
    fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
