package dev.sku20.stopgap.helidon.authentication

import io.helidon.common.context.Context
import io.helidon.http.HttpException
import io.helidon.http.Status
import io.helidon.webserver.http.FilterChain
import io.helidon.webserver.http.RoutingRequest
import io.helidon.webserver.http.RoutingResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthenticationFilterTest {

    private val resolver = mock<AuthenticationResolver>()
    private val filter = AuthenticationFilter(resolver)
    private val chain = mock<FilterChain>()
    private val req = mock<RoutingRequest>()
    private val res = mock<RoutingResponse>()
    private val context = Context.create()

    private class TestAuthN : Authentication

    @Test
    fun registersAuthenticationAndProceeds() {
        val testAuth = TestAuthN()
        whenever(req.context()).thenReturn(context)
        whenever(resolver.authenticate(req)).thenReturn(testAuth)

        filter.filter(chain, req, res)

        assertThat(context.get(Authentication::class.java)).contains(testAuth)
        verify(chain).proceed()
    }

    @Test
    fun propagatesExceptionWhenResolverThrows() {
        val exception = HttpException("Unauthorized", Status.UNAUTHORIZED_401)
        whenever(resolver.authenticate(req)).thenThrow(exception)

        assertThatThrownBy { filter.filter(chain, req, res) }
            .isSameAs(exception)

        verify(chain, never()).proceed()
    }
}
