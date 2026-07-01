package dev.sku20.stopgap.ir

class NoCreatorException(qualifier: String) : RuntimeException(
    "no creator registered for qualifier: $qualifier"
)
