package dev.sku20.stopgap.ir

class CreatorExistsException(qualifier: String) : RuntimeException(
    "creator already exists for qualifier: $qualifier"
)
