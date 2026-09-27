package dev.sku20.stopgap.app.authn

import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver
import io.helidon.http.HeaderNames
import io.helidon.webserver.http.ServerRequest

/**
 * Application-level implementation of [AuthenticationResolver].
 *
 * ### Architectural Pattern: Security Validation Adapters
 * Stopgap avoids running parallel web-layer security middleware (e.g. Helidon's
 * `SecurityFeature` / `WebSecurity` route filters). Instead, Stopgap uses an **Adapter pattern**:
 * - Third-party or framework libraries (Helidon Security, Nimbus, JJWT, etc.) are used purely as
 *   **validation engines** inside the resolver.
 * - The [Authentication] model is owned entirely by the application domain (e.g. [UserAuthN],
 *   [AnonymousAuthN]), with the adapter mapping validated claims or subjects into these domain types.
 *
 * Below are two reference examples of how Helidon validation utilities can be adapted:
 *
 * #### Example 1: Direct JWT / JWKS Validation Adapter
 * Uses `io.helidon.security.jwt.SignedJwt` and `JwkKeys` strictly for cryptographic verification:
 * ```kotlin
 * class HelidonJwtAuthenticationResolver(
 *     private val jwkKeys: JwkKeys,
 * ) : AuthenticationResolver {
 *     override fun authenticate(request: ServerRequest): Authentication {
 *         val authHeader = request.headers().first(HeaderNames.AUTHORIZATION).orElse(null)
 *             ?: return AnonymousAuthN
 *
 *         if (!authHeader.startsWith("Bearer ")) {
 *             return AnonymousAuthN
 *         }
 *         val rawToken = authHeader.removePrefix("Bearer ").trim()
 *
 *         return try {
 *             val signedJwt = SignedJwt.parseToken(rawToken)
 *             val errors = signedJwt.verifySignature(jwkKeys)
 *             if (errors.hasErrors()) {
 *                 throw HttpException("Invalid token signature", Status.UNAUTHORIZED_401)
 *             }
 *             val jwt = signedJwt.jwt()
 *             val userId = jwt.subject().orElse(null)
 *                 ?: throw HttpException("Missing subject claim", Status.UNAUTHORIZED_401)
 *             UserAuthN(id = userId)
 *         } catch (e: Exception) {
 *             throw HttpException("Invalid token", Status.UNAUTHORIZED_401)
 *         }
 *     }
 * }
 * ```
 *
 * #### Example 2: Helidon `Security` Engine Adapter
 * Uses `io.helidon.security.Security` programmatically with configured providers (e.g. JWT, OIDC):
 * ```kotlin
 * class HelidonSecurityAuthenticationResolver(
 *     private val security: Security,
 * ) : AuthenticationResolver {
 *     override fun authenticate(request: ServerRequest): Authentication {
 *         // 1. Build SecurityEnvironment from ServerRequest
 *         val env = SecurityEnvironment.builder()
 *             .path(request.path().rawPath())
 *             .method(request.prologue().method().text())
 *             .headers(request.headers())
 *             .build()
 *
 *         // 2. Programmatically evaluate security without WebServer middleware
 *         val secContext = security.createContext(env)
 *         val secResponse = secContext.authenticate().toCompletableFuture().join()
 *
 *         return when (secResponse.status()) {
 *             SecurityResponse.SecurityStatus.SUCCESS -> {
 *                 val subject = secContext.user().orElse(null)
 *                 val userId = subject?.principal()?.id() ?: "unknown"
 *                 UserAuthN(id = userId)
 *             }
 *             SecurityResponse.SecurityStatus.FAILURE -> {
 *                 throw HttpException("Authentication failed", Status.UNAUTHORIZED_401)
 *             }
 *             else -> AnonymousAuthN
 *         }
 *     }
 * }
 * ```
 */
class AppAuthenticationResolver : AuthenticationResolver {

    override fun authenticate(request: ServerRequest): Authentication {
        if (request.headers().contains(HeaderNames.AUTHORIZATION)) {
            val authHeader = request.headers().get(HeaderNames.AUTHORIZATION).get()
            if (authHeader.startsWith("Bearer ")) {
                val token = authHeader.removePrefix("Bearer ").trim()
                if (token.isNotEmpty()) {
                    return UserAuthN(id = token)
                }
            }
        }
        return AnonymousAuthN
    }
}
