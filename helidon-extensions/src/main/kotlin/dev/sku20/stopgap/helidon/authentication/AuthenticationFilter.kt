package dev.sku20.stopgap.helidon.authentication

import io.helidon.webserver.http.Filter
import io.helidon.webserver.http.FilterChain
import io.helidon.webserver.http.RoutingRequest
import io.helidon.webserver.http.RoutingResponse

class AuthenticationFilter(
    private val resolver: AuthenticationResolver
) : Filter {

    override fun filter(chain: FilterChain, req: RoutingRequest, res: RoutingResponse) {
        req.context().supply(Authentication::class.java) {
            resolver.authenticate(req)
        }
        chain.proceed()
    }
}
