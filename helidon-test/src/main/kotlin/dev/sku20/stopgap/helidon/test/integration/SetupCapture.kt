package dev.sku20.stopgap.helidon.test.integration

import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver

typealias TestInstances = Map<Class<*>, Any>

class SetupCapture(
    val endpoint: Any,
    val authResolver: AuthenticationResolver = TestAuthFakeResolver(),
    val registerParams: Array<Any> = emptyArray(),
    val instances: TestInstances = emptyMap(),
)
