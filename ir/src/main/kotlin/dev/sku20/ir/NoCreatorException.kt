package dev.sku20.ir

class NoCreatorException(qualifier: String) : RuntimeException(
    "no creator registered for qualifier: $qualifier"
)
