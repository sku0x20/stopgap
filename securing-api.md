# RFC: Type-Safe API Authentication (AuthN)

- **Status**: Proposed
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

When the parameter is omitted:
- **Enforcement with Default**: The check falls back to a build-time configured default type (configured via KSP). The framework validates that the resolved principal matches the default type (throwing `403 Forbidden` on mismatch), but invokes the endpoint method without passing the instance. This keeps method signatures clean when caller identity is not needed in the body.
- **Compile-Time Validation**:
  - If the configured default type names a class that does not exist, KSP fails the build at compile-time.
  - If no default type is configured and an endpoint method omits the parameter, KSP fails the build at compile-time. Because the route would be functionally unreachable at runtime without an expected type to match against, it fails early during compilation rather than deferring to a runtime failure.

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

---

## 7. FAQ

**Q: Can a resolver validate both client IP and token claims?**  
Yes. The developer has full access to the `ServerRequest` inside `authenticate(request)` and can enforce any combination of request attributes.

**Q: Does omitting the parameter make an endpoint public?**  
No. Omitting the parameter falls back to the configured build-time default type. To make an endpoint public, declare the application's unauthenticated type (e.g., `auth: NoAuthN`) explicitly.
