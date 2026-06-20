package extension.webservertest

class SetupCapture(
    val endpoint: Any,

    // params for the generated register method
    val registerParams: Array<Any> = emptyArray(),

    val instances: Map<Class<*>, Any> = emptyMap(),
)
