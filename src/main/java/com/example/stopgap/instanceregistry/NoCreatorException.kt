package com.example.stopgap.instanceregistry

class NoCreatorException(qualifier: String) : RuntimeException(
    "no creator registered for qualifier: $qualifier"
)
