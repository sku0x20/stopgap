# Stopgap

Nothing is more permanent than a temporary solution. If you're building something quick, keep it cheap:
minimal to put together, and just as easy to take apart later.

A small, compile-time Kotlin toolkit for building HTTP services on [Helidon Core](https://helidon.io/) (formerly Helidon SE).

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

## Testing is first class

Stopgap came out of necessity, when even Helidon didn't provide the kind of testing support it needed.
The toolkit grew from there, but testing stayed at the center, and Stopgap itself is built
test-first.

**No colored functions, so no test ceremony.** There's no `runBlocking`, no `runTest`, no test dispatchers,
and no virtual time. Endpoints are ordinary blocking code, so tests are ordinary blocking code.

**Integration and end-to-end tests are kept apart.** Most frameworks blur the two. The Gradle plugin sets up
`intTest` and `e2eTest` as separate suites, each with its own job.

**Integration tests via `@WebserverTest`** start a minimal Helidon server with endpoint and required dependencies.
As a rule of thumb, never fake what you're testing, it's `integration testing`, not mock testing. 
So the server is never mocked: requests travel over real HTTP, through the generated routing, to endpoint under test.
Nothing is static, so integration tests can run in parallel. In other frameworks, 
int tests usually try to fit both e2e tests and integration tests, so they mostly have static state shared across tests.

**End-to-end tests: docker image as a black box.** The E2E suite builds your Docker image, boots it on a Docker
network, and tests it from the outside. It's the exact image you deploy, JVM or native. What you test is what
you ship. E2E tests usually run sequentially.

## Why not Ktor?

Ktor is built on coroutines, which means colored functions: `suspend` spreads through your code, and you
have to think about dispatchers. Stopgap runs on Helidon, whose web server was rewritten from scratch for
virtual threads. You write plain blocking code, like Go, and the JVM schedules it. There's no `suspend`, no
dispatchers, and no reactive engine converted to green threads underneath. As structured concurrency matures
in Java, you can adopt it directly.

## Why not http4k?

http4k is an abstraction over many servers, and you learn its model (lenses, filters). 
Its style is heavily functional: handlers are functions, composed with filters. Stopgap is plain, everyday Kotlin.
Its tests usually call the app in memory, skipping the server and HTTP entirely. Stopgap's integration tests always go through a real server.
Routing is generated at compile time as plain Kotlin you can read and step through.

## Modules

Every module is optional.

| Module               | What it gives you                                                               |
|----------------------|---------------------------------------------------------------------------------|
| `ir`                 | Compile-time dependency wiring with `@Creates`                                  |
| `helidon-extensions` | `@Endpoint` routing, parameter binding, content negotiation, and authentication |
| `helidon-test`       | JUnit extensions for in-process integration tests and Docker E2E tests          |
| `gradle-plugin`      | Thin-jar packaging, Docker image tasks, and `intTest`/`e2eTest` suites          |

## Getting started

Requires JDK 25+, Kotlin 2.4, and KSP 2.3. See [`app/build.gradle.kts`](app/build.gradle.kts) for a complete setup.

## Examples

The [`app`](app) module is a working example of every feature. It's more than documentation: it's also the
integration suite for Stopgap itself. Every feature in it runs through the real code generation, and its
tests prove it works. The examples can't go stale, because if they break, the build fails. Tests as
documentation, the way TDD intends.

## License

[MIT](LICENSE)

---

*Built for developers who value transparency, performance, and simplicity.*
