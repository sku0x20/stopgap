# Stopgap

A lightweight **Helidon SE** template for **Kotlin** with **Project Loom** virtual threads.

No runtime reflection. No hidden containers. Handlers are plain blocking functions — the JVM schedules them on virtual threads.

## Modules

| Module | Description |
|:---|:---|
| `ir` | Compile-time DI via KSP. Generates wiring at build time — plain function calls at runtime, no classpath scanning. |
| `helidon-extensions` | Routing annotations (`@Get`, `@Post`, …) with KSP codegen for Helidon route registration. Includes param binding and a serde layer. |
| `helidon-test` | JUnit 5 extensions for integration and E2E testing. Injects `WebClient` directly into test constructors. |
| `gradle-plugin` | Sets up copyLibs, jar, Docker build, and test suites from one plugin block. |
| `app` | Starter project wiring all modules together. |

## Maven Central

```kotlin
implementation("dev.sku20.stopgap:ir:2.8.0")
implementation("dev.sku20.stopgap:helidon-extensions:2.8.0")
implementation("dev.sku20.stopgap:helidon-test:2.8.0")
id("dev.sku20.stopgap") version "2.8.0"
```

## Testing

Three tiers, all wired through JUnit 5 extensions:

| Level | Description |
|:---|:---|
| **Unit** | Plain JUnit, no infrastructure. |
| **Integration** | Spins up a real Loom-based server in-process. `WebClient` injected into the test constructor. |
| **E2E** | Builds and runs the Docker image via Testcontainers. `WebClient` injected, full lifecycle managed. |

## Getting Started

1. Explore `:app` src and test directories.
2. Build with the Gradle wrapper.
3. Configuration via YAML (`helidon-config-yaml`).
