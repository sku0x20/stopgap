package dev.sku20.ir

class CreatorExistsException(qualifier: String) : RuntimeException(
    "creator already exists for qualifier: $qualifier"
)
