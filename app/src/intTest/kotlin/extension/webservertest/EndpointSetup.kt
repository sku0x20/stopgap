package extension.webservertest

import dev.sku20.helidon.serde.Serde

data class EndpointSetup(
    val endpoint: Any,
    val serde: Serde,
)
