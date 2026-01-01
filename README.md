# 🛑 Stopgap

A high-performance, lightweight **Helidon SE** template designed for modern Java development with **Project Loom**.

## ✨ Features

- **🚀 Helidon SE (Nima) 4.x:** Built on the minimal, code-first API surface.
- **🧵 Virtual Thread Native:** No async mess—handlers are plain, blocking Java methods running on virtual threads.
- **🛡️ Zero Magic:** No runtime reflection magic or hidden containers.
- **📦 Minimal Footprint:** Keeping dependencies to an absolute minimum for fast startup and low memory usage.
- **⚙️ Custom DI:** Includes `ir` (Instance Registry) with lightweight codegen instead of a heavy DI framework.

## 🏗️ Project Structure

- `app/`: The core template and starter project.
- `ir/`: Instance Registry with source-generated dependency injection.
- `helidon-extensions/`: Custom codegen and Helidon utilities.

## 🧪 Testing Strategy

The project features a robust, tiered testing setup:

| Level           | Description                                                                                                              |
|:----------------|:-------------------------------------------------------------------------------------------------------------------------|
| **Unit**        | Fast, isolated tests for business logic.                                                                                 |
| **Integration** | Uses `WebServerTestExtension` to spin up a Loom-based webserver for endpoint testing.                                    |
| **E2E**         | Full lifecycle testing: Builds a Docker image via Gradle and runs it using a JUnit extension with `WebClient` injection. |

---

### Getting Started

1. **Explore:** Check out `:app` src and test directories.
2. **Build:** Use standard Gradle wrappers.
3. **Run:** Configuration is handled via YAML (`helidon-config-yaml`).

---
*Built for developers who value transparency, performance, and simplicity.*
