package extension.webservertest

typealias TestInstances = Map<Class<*>, Any>

class SetupCapture(
    val endpoint: Any,

    // params for the generated register method
    val registerParams: Array<Any> = emptyArray(),

    val instances: TestInstances = emptyMap(),
)
