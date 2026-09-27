# Stopgap

A small, compile-time Kotlin toolkit for building HTTP services on [Helidon Core](https://helidon.io/) (formerly Helidon SE).

Stopgap is not a framework. It doesn't own your `main()`, start a container, or scan your classpath. It's a set of
separate libraries. Each one removes a specific piece of boilerplate by generating plain Kotlin at compile time, and
then steps aside. You use the ones you want and ignore the rest.

## Why Stopgap

Big frameworks are convenient until you need to understand what they're doing. Startup runs through reflection and
classpath scanning. The call path from socket to handler goes through layers you never wrote. A BOM decides which
versions of your libraries you're allowed to use, and an "HTTP framework" ends up choosing your database access,
validation, and application architecture for you.

Stopgap takes the opposite approach.

- **You own the control flow.** You write `main()`. You build the `WebServer`. Nothing runs that you can't find by
  following calls from `main()`.
- **Nothing is hidden.** There's no runtime reflection, no proxies, and no classpath scanning. KSP generates ordinary
  Kotlin that you can read, set a breakpoint in, and step through.
- **An HTTP toolkit is only an HTTP toolkit.** Stopgap is a thin layer over Helidon's web server. It brings no
  database layer, no validation, and no JSON library. You pick those yourself.
- **No BOM.** Every module is published and versioned on its own. You declare Helidon yourself and choose its version.
- **Tests with as little magic as possible.** Integration tests start a real server around an endpoint *you*
  construct. End-to-end tests run your actual Docker image.

## Modules

Every module is optional.

| Module               | What it gives you                                                               |
|----------------------|---------------------------------------------------------------------------------|
| `ir`                 | Compile-time dependency wiring with `@Creates`                                  |
| `helidon-extensions` | `@Endpoint` routing, parameter binding, content negotiation, and authentication |
| `helidon-test`       | JUnit extensions for in-process integration tests and Docker E2E tests          |
| `gradle-plugin`      | Thin-jar packaging, Docker image tasks, and `intTest`/`e2eTest` suites          |

## Getting started

Requires JDK 25+, Kotlin 2.4, and KSP 2.3.

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.3.12"
    id("dev.sku20.stopgap") version "3.0.0"    // from Maven Central
}

dependencies {
    implementation("io.helidon.webserver:helidon-webserver:4.5.5")

    implementation("dev.sku20.stopgap:ir:3.0.0")
    ksp("dev.sku20.stopgap:ir:3.0.0")
    implementation("dev.sku20.stopgap:helidon-extensions:3.0.0")
    ksp("dev.sku20.stopgap:helidon-extensions:3.0.0")

    testImplementation("dev.sku20.stopgap:helidon-test:3.0.0")
}

ksp {
    arg("stopgap.codegen.endpoint.auth.defaultType", "public")
}
```

```kotlin
@Endpoint("/users")
class UserEndpoint(private val users: UserService) {

    @Get("/{id}")
    fun get(@PathParam("id") id: String): UserDto = users.find(id)
}
```

## Examples

The [`app`](app) module is a working example of every feature:

| Where                                      | What                                      |
|--------------------------------------------|-------------------------------------------|
| [`Main.kt`](app/src/main/kotlin/dev/sku20/stopgap/app/Main.kt), [`RootConfig.kt`](app/src/main/kotlin/dev/sku20/stopgap/app/RootConfig.kt) | `main()` and wiring |
| [`param/`](app/src/main/kotlin/dev/sku20/stopgap/app/param)       | Path, query, and header parameters |
| [`serde/`](app/src/main/kotlin/dev/sku20/stopgap/app/serde)       | Serialization and content negotiation |
| [`authn/`](app/src/main/kotlin/dev/sku20/stopgap/app/authn)       | Authentication |
| [`exception/`](app/src/main/kotlin/dev/sku20/stopgap/app/exception) | Error responses |
| [`generator/`](app/src/main/kotlin/dev/sku20/stopgap/app/generator) | Qualifiers |
| [`src/intTest`](app/src/intTest)                                  | Integration tests with `@WebserverTest` |
| [`src/e2eTest`](app/src/e2eTest)                                  | Docker end-to-end tests |
| [`build.gradle.kts`](app/build.gradle.kts), [`Dockerfile`](app/Dockerfile) | Build and plugin setup |

## License

[MIT](LICENSE)

---

*Built for developers who value transparency, performance, and simplicity.*
