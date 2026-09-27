package dev.sku20.stopgap.helidon.authentication

import io.helidon.common.context.Context
import io.helidon.http.HttpException
import io.helidon.http.Status
import io.helidon.webserver.http.FilterChain
import io.helidon.webserver.http.RoutingRequest
import io.helidon.webserver.http.RoutingResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
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

    @BeforeEach
    fun setup() {
        whenever(req.context()).thenReturn(context)
    }

    @Test
    fun suppliesAuthenticationLazilyAndProceeds() {
        val testAuth = TestAuthN()
        whenever(resolver.authenticate(req)).thenReturn(testAuth)

        filter.filter(chain, req, res)

        verify(chain).proceed()
        verify(resolver, never()).authenticate(any())

        val resolved = context.get(Authentication::class.java)
        assertThat(resolved).contains(testAuth)
        verify(resolver, times(1)).authenticate(req)
    }

    @Test
    fun memoizesAuthenticationWhenAccessedMultipleTimes() {
        val testAuth = TestAuthN()
        whenever(resolver.authenticate(req)).thenReturn(testAuth)

        filter.filter(chain, req, res)

        context.get(Authentication::class.java)
        context.get(Authentication::class.java)

        verify(resolver, times(1)).authenticate(req)
    }

    @Test
    fun propagatesExceptionWhenResolverThrowsOnAccess() {
        val exception = HttpException("Unauthorized", Status.UNAUTHORIZED_401)
        whenever(resolver.authenticate(req)).thenThrow(exception)

        filter.filter(chain, req, res)
        verify(chain).proceed()

        assertThatThrownBy { context.get(Authentication::class.java) }
            .isSameAs(exception)
    }
}
