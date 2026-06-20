package extension.webservertest

class SetupCapture(
    val endpoint: Any,

    // params for the generated register method
    val registerParams: Array<Any>,

    val instances: Map<Class<*>, Any>,
)
