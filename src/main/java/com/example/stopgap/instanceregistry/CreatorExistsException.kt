package com.example.stopgap.instanceregistry

class CreatorExistsException(qualifier: String) : RuntimeException(
    "creator already exists for qualifier: $qualifier"
)
