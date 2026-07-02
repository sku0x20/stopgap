# 🛑 Stopgap

A high-performance, lightweight **Helidon SE** template designed for modern **Kotlin** development with **Project Loom**.

## ✨ Features

- **🚀 Helidon SE (Nima) 4.x:** Built on the minimal, code-first API surface.
- **🧵 Virtual Thread Native:** No async mess—handlers are plain, blocking Kotlin functions running on virtual threads.
- **🛡️ Zero Magic:** No runtime reflection magic or hidden containers.
- **📦 Minimal Footprint:** Keeping dependencies to an absolute minimum for fast startup and low memory usage.
- **⚙️ Custom DI:** Includes `ir` (Instance Registry) with KSP-based compile-time codegen instead of a heavy DI framework.

## 🏗️ Project Structure

- `app/`: The core template and starter project.
- `ir/`: Instance Registry with source-generated dependency injection.
- `helidon-extensions/`: Routing annotations, KSP codegen for Helidon routes, param binding, and serde layer.
- `helidon-test/`: JUnit 5 extensions for integration and E2E testing.
- `gradle-plugin/`: Gradle plugin that wires up the full build pipeline.

## 📦 Maven Central

```kotlin
implementation("dev.sku20.stopgap:ir:2.8.0")
implementation("dev.sku20.stopgap:helidon-extensions:2.8.0")
implementation("dev.sku20.stopgap:helidon-test:2.8.0")
id("dev.sku20.stopgap") version "2.8.0"
```

## 🧪 Testing Strategy

The project features a robust, tiered testing setup:

| Level           | Description                                                                                                              |
|:----------------|:-------------------------------------------------------------------------------------------------------------------------|
| **Unit**        | Fast, isolated tests for business logic.                                                                                 |
| **Integration** | Spins up a real Loom-based webserver in-process. `WebClient` injected into the test constructor.                         |
| **E2E**         | Full lifecycle testing: Builds a Docker image via Gradle and runs it using a JUnit extension with `WebClient` injection. |

---

### Getting Started

1. **Explore:** Check out `:app` src and test directories.
2. **Build:** Use standard Gradle wrappers.
3. **Run:** Configuration is handled via YAML (`helidon-config-yaml`).

---
*Built for developers who value transparency, performance, and simplicity.*
