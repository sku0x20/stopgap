package dev.sku20.stopgap.helidon.test.integration.extension

typealias TestInstances = Map<Class<*>, Any>

class SetupCapture(
    val endpoint: Any,
    val registerParams: Array<Any> = emptyArray(),
    val instances: TestInstances = emptyMap(),
)
