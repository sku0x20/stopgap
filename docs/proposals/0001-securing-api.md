# RFC-0001: Type-Safe API Authentication (AuthN)

- **Date**: 2026-09-27
- **Target**: `stopgap` / `helidon-extensions`
- **Scope**: Authentication (AuthN) API Contract & Type-Enforcement

---

## 1. Summary

This proposal establishes a minimal, opinionated authentication (AuthN) mechanism for Stopgap APIs. Rather than relying on annotations or ad-hoc validation inside handlers, endpoints declare the principal type they require as a standard method parameter.

The framework provides two minimal interfaces: `Authentication` and `AuthenticationResolver`. It runs the registered resolver upstream on incoming requests and enforces type compatibility against the endpoint's declared parameter, yielding a `403 Forbidden` on mismatch.

---

## 2. Motivation

Ad-hoc authentication validation violates DRY—boilerplate token extraction and identity checks leak into every endpoint handler.

Stopgap aims to solve this with zero magic:
- **No annotations**: The method signature itself is the declaration.
- **Strong typing**: Endpoints receive the resolved principal directly as a typed argument.
- **Fail-fast**: Mismatches are rejected at the framework boundary before handler code executes.

---

## 3. Goals & Non-Goals

### Goals
- Establish a single, uniform AuthN pipeline for all endpoints.
- Allow endpoint methods to declare their expected principal via a parameter type.
- Enforce type matching upstream, converting mismatches into `403 Forbidden`.
- Support build-time defaults for methods that omit the parameter.

### Non-Goals
- **Authorization (AuthZ)**: Access control policies (permissions, resource ownership) depend on domain entities and business logic. They belong inside domain services, not at the transport boundary.
- **Built-in Principal Types**: The framework provides no concrete types (e.g., no default `UserAuthN` or `NoAuthN`). All concrete implementations are owned by the application.
- **Composite/Delegating Resolvers**: The framework registers one global resolver. If an application requires multi-strategy authentication, it coordinates them inside its own resolver implementation.
- **Browser Challenges (`WWW-Authenticate`)**: REST endpoints are caller-driven; browser challenge flows are out of scope.

---

## 4. API Specification

The framework defines only two interfaces:

```kotlin
interface Authentication

interface AuthenticationResolver {
    fun authenticate(request: ServerRequest): Authentication
}
```

Both interfaces are intentionally empty:
- `Authentication` is a marker interface. Concrete implementations (`UserAuthN`, `MachineAuthN`, `NoAuthN`, etc.) and their fields are defined entirely by the application developer.
- `AuthenticationResolver` receives the raw request and produces an `Authentication` instance. Because Stopgap runs on virtual threads (Project Loom) with high concurrency, the resolver is invoked concurrently across requests and must be thread-safe (or stateless).

---

## 5. Detailed Design

### 5.1 Endpoint Declarations
An endpoint declares its required identity by accepting an implementation of `Authentication` as a parameter:

```kotlin
@Endpoint("/users")
class UserEndpoint {

    // Requires UserAuthN
    @Get("/profile")
    fun getProfile(auth: UserAuthN): UserProfile { ... }

    // Public endpoint: explicitly declares the app's unauthenticated type
    @Get("/health")
    fun health(auth: NoAuthN): HealthStatus { ... }
}
```

Because the framework has no built-in knowledge of what "public" or "unauthenticated" means, public endpoints declare the application's unauthenticated type (e.g., `NoAuthN`) and undergo the exact same type-match check.

### 5.2 Resolution & Enforcement Lifecycle
1. **Global Execution**: A single `AuthenticationResolver` is registered globally. It executes upstream against incoming requests before dispatch.
2. **Type Matching**: The framework inspects the concrete `Authentication` instance returned by the resolver and checks it against the endpoint parameter type using an `is TargetType` check.
3. **Dispatch**:
   - If the resolved instance matches, it is passed to the method parameter and the handler executes.
   - If there is a mismatch, the framework immediately halts the request and throws `HttpException("Forbidden", Status.FORBIDDEN_403)`.

### 5.3 Error Handling & Status Codes
The responsibility for error codes is strictly partitioned between the application developer and the framework:

| Scenario | Handled By | HTTP Status | Rationale |
| :--- | :--- | :--- | :--- |
| **Token Expired / Malformed / Invalid** | Developer's `AuthenticationResolver` | **`401 Unauthorized`** | **Application responsibility**: When credentials are provided but fail validation, the resolver throws 401 directly to trigger client-side re-auth or token refresh flows. |
| **AuthN Type Mismatch** | Framework | **`403 Forbidden`** | **Framework responsibility**: The resolver returned an `Authentication` instance that does not match the endpoint's declared parameter type (this includes when an anonymous type is supplied to a protected endpoint). |

Note: this intentionally treats a missing-credentials request the same as any other type mismatch — anonymous is just another `Authentication` type that fails the check. `401` is reserved for the narrower case where credentials were supplied but rejected by the resolver. This departs from strict HTTP semantics (where absent credentials often imply `401`), but keeps the enforcement boundary a single, uniform type check.

### 5.4 Omitted Parameter & Build-Time Defaults
An endpoint method is not required to declare an `Authentication` parameter if its body does not need caller identity.

The behavior for methods that omit the parameter is governed by a single build-time KSP option:
`stopgap.codegen.endpoint.auth.defaultType`

- **When set to `"public"`**:
  Methods that omit the `Authentication` parameter are open/public. The framework generates no authentication check for them, allowing the lazy resolver to be completely bypassed.
- **When set to a concrete `Authentication` class FQN (e.g., `"com.example.UserAuthN"`)**:
  Methods that omit the parameter are automatically protected by that default type. The framework checks that the resolved principal matches the default type (throwing `403 Forbidden` on mismatch), but invokes the endpoint method without passing the parameter. This keeps method signatures clean when caller identity is not needed in the handler body.
  - If the configured default class does not exist or does not implement `Authentication`, KSP fails the build at compile-time.
- **When omitted / not configured**:
  If an endpoint method omits the `Authentication` parameter and no `defaultType` is configured, KSP fails compilation with an `IllegalArgumentException`. Developers must either declare an explicit `Authentication` parameter on the method or configure `stopgap.codegen.endpoint.auth.defaultType` (e.g. `"public"` or a concrete `Authentication` FQN). This prevents accidental exposure and provides immediate build-time feedback rather than unexpected runtime errors.

---

## 6. Design Rationale & Alternatives Considered

### Why No Built-in `NoAuthN` or Route Skipping?
Skipping the resolver for public endpoints would require the framework to ship with a built-in `NoAuthN` type or special-case marker annotations. Keeping the framework completely unopinionated about principal types means the resolver must run unconditionally. What an anonymous request resolves to is purely the resolver’s domain.

### Why Parameter Types Instead of Annotations?
Annotations like `@Authenticated` or `@Public` introduce a disconnected declaration layer. Parameter types achieve two goals simultaneously:
1. They act as the declarative gatekeeper.
2. They deliver the typed identity object directly into the method body with zero casting or context lookups.

### Single Concrete Types vs. Multi-Principal Endpoints
Endpoint parameters accept a single concrete type checked via an `is` check. The library does not manage union types or sealed hierarchies at the routing boundary. If an endpoint needs to be accessible by multiple caller kinds (e.g., either a user or a service worker), the developer defines a concrete composite type (e.g., `CallerAuthN`) and performs any necessary internal branching inside the handler.

### Single Global Resolver vs. Resolver Chains
The framework avoids built-in composite or delegating resolver chains. Chaining rules ("first match wins", fallback rules) add hidden complexity. The application registers one resolver; if multi-source resolution is needed (e.g. Bearer tokens vs. API keys), the developer organizes that logic within their own resolver.

### Security Validation Adapters (Decoupled Validation vs. Web Middleware)
Stopgap intentionally does **not** run parallel web-layer security frameworks (e.g., Helidon WebServer's `SecurityFeature` / `WebSecurity`). Running parallel route filters or interceptors creates fragmented routing logic, duplicate middleware overhead, and coupled framework-specific principal types.

Instead, external validation tools (such as Helidon Security, Helidon JWT/JWKS, Nimbus JOSE, or JJWT) are integrated using the **Adapter Pattern**:
1. **Validation Engine Only**: The framework's validation engine is used strictly inside the application's `AuthenticationResolver` to verify token signatures, validate claims, or invoke authentication providers.
2. **Domain-Owned `Authentication` Types**: Concrete `Authentication` types belong strictly to the application domain (`UserAuthN`, `ServiceAuthN`, `AnonymousAuthN`). The resolver acts as an adapter that converts verified tokens or subjects into these domain objects.
3. **Pluggable & Replaceable**: Applications can swap or upgrade validation libraries (e.g., migrating from Helidon JWT to Nimbus or a custom introspector) without altering endpoint handler signatures or routing rules.

---

## 7. Security Considerations

### Route Enumeration (404 vs. 403)
Because route matching evaluates the path before the method's `Authentication` type is verified:
- Probing a non-existent path yields `404 Not Found`.
- Probing an existing protected path without valid credentials yields `403 Forbidden`.

This distinction inherently reveals the existence of protected endpoints to unauthenticated callers via status code probing. This proposal intentionally embraces the standard REST approach (returning `403` on existing protected endpoints):
1. **Simplicity in Design & Implementation**: Masking `403` as `404` ("stealth mode") would require the framework to track unauthenticated states or introduce artificial routing hooks. Leaving them distinct keeps the framework's routing and type checking simple and predictable.
2. **Clear Diagnostics**: Returning `403 Forbidden` provides clear, honest diagnostics to legitimate API consumers that the endpoint exists but requires credentials.
3. **Security over Obscurity**: Masking whether an endpoint exists provides little practical security benefit. Real protection comes from strict, fail-closed verification at the boundary, not from concealing route presence.

---

## 8. FAQ

**Q: Can a resolver validate both client IP and token claims?**  
Yes. The developer has full access to the `ServerRequest` inside `authenticate(request)` and can enforce any combination of request attributes.

**Q: Does omitting the parameter make an endpoint public?**  
Only if `stopgap.codegen.endpoint.auth.defaultType` is explicitly set to `"public"`. If set to an `Authentication` class FQN, it protects with that type; if not configured at all, omitting the parameter results in a compile-time failure.
