# RFC-0002: Distributed Tracing

- **Date**: 2026-10-23
- **Target**: `stopgap` / `app`
- **Scope**: Traces only (metrics, logs and profiles are separate proposals)

---

## 1. Summary

Helidon traces the HTTP edge with its own instrumentation. The OpenTelemetry (OTel) Java agent runs with every library
instrumentation disabled and has one job: turning `@WithSpan` annotations on our code into spans. Both write to one
OTel SDK, the agent's, which Helidon reuses through an explicitly built `Tracer`. They share one `Context`, so a request
yields one trace from the server span down to our business methods.

The guiding principle is **thin, decoupled layers**: each layer does one job, with as little hidden behavior as possible.
Removing the agent should leave the Helidon setup nearly untouched; we'd lose only the annotated spans.

---

## 2. Goals & Non-Goals

### Goals
- One server span per request, continuing an incoming `traceparent`.
- Spans for chosen business methods, declared with annotations rather than tracing code.
- A single SDK and exporter for all spans.
- Thin layers: Helidon traces the edge, and the agent only processes annotations. Neither depends on the other's internals.
- Application code depends only on the standard OTel API and annotations.

### Non-Goals
- **Library auto-instrumentation** (JDBC, HTTP clients, Helidon's server): off; each is a deliberate later opt-in.
- **Span per method**: method-level cost belongs to profiling, not tracing.
- **Metrics, logs, profiles**: separate proposals; this one only keeps the door open for them.

---

## 3. Design

```
                    single OTel SDK + exporter
                     ▲                      ▲
Helidon TracingObserver              @WithSpan on our methods
(server span per request)            (agent: annotations only)
     └──── same Context (request virtual thread's ThreadLocal) ────┘
```

### 3.1 Helidon: the HTTP edge
- `helidon-webserver-observe-tracing`: server span per request; extracts and continues `traceparent`.
- `helidon-tracing-providers-opentelemetry`: backs Helidon's tracing API with OTel.
- Wired explicitly through our registry (`TracingConfig`, `ObserveConfig`); no service discovery.

### 3.2 SDK ownership: the agent's SDK, Helidon reuses it
The agent always runs its own SDK and can't adopt one built by the app, so it owns the single SDK. `TracingConfig`
builds Helidon's `Tracer` on top of it:
```kotlin
@Creates
fun tracer(config: Config): Tracer {
    if (HelidonOpenTelemetry.AgentDetector.isAgentPresent(config)) {
        val openTelemetry = GlobalOpenTelemetry.get()
        return HelidonOpenTelemetry.create(openTelemetry, openTelemetry.getTracer("io.helidon.tracing"), mapOf())
    }
    return TracerBuilder.create(config.get("tracing")).build()
}
```
- **Agent attached**: Helidon wraps the agent's `GlobalOpenTelemetry`.
- **No agent**: Helidon builds its own SDK from the `tracing:` config node.
- **Forced, not guessed**: `otel.agent.present: true` in `application.yaml` replaces Helidon's heuristic detection.

### 3.3 Agent: annotations only
Configured from a file in the repo, `app/otel.properties`, passed with
`-Dotel.javaagent.configuration-file=otel.properties`. The `:app:run` task resolves the agent from Maven and attaches
it with that file.
```
otel.service.name=stopgap
otel.instrumentation.common.default-enabled=false
otel.instrumentation.opentelemetry-api.enabled=true                          # bridges app API calls to the agent's context
otel.instrumentation.opentelemetry-instrumentation-annotations.enabled=true  # @WithSpan, @SpanAttribute
otel.instrumentation.executors.enabled=true                                  # carries Context into tasks on our executors

otel.traces.exporter=otlp
otel.exporter.otlp.protocol=http/protobuf
otel.exporter.otlp.traces.endpoint=http://localhost:10428/insert/opentelemetry/v1/traces   # VictoriaTraces
otel.metrics.exporter=none
otel.logs.exporter=none
```

### 3.4 Application code
```kotlin
@WithSpan
fun sync(
    @SpanAttribute("note.id")
    noteId: Uuid
): Note {
```
- **Add attributes before adding spans**: `Span.current().setAttribute("note.id", id)` adds detail to the active span.
- **New span**: only for a unit with its own latency or failure mode, or one that fans out.
- **Manual OTel API**: fallback for spans that don't map to a method (e.g. per iteration of a loop).

---

## 4. Why This Approach

### Why thin layers?
Each layer we add is behavior we can't see in our code. Keeping the agent's role narrow means the server's tracing
is plain Helidon code we wired ourselves, and the agent can be removed at the cost of the annotated spans only.

### Why annotations?
Tracing is a cross-cutting concern. An annotation states the intent and keeps business code free of tracing code
(`try`/`finally`/`end()`/scope handling).

### Why the agent for annotations?
- **No proxies**: the agent rewrites the method's own bytecode, so self-invocation and Kotlin's final classes are
  traced. Proxy-based AOP (Spring style) misses both.
- **No runtime cost in tests**: without the agent the annotations are inert, so unit tests run untouched.
- **Helidon's `@Tracing.Traced` isn't usable here**: it relies on Helidon's Java codegen and service registry, and
  our endpoints are generated by our own KSP codegen.

### Why Helidon for the server span, not the agent?
The agent has a Helidon server module (`helidon-4.3`), but using it would put the agent's bytecode rewriting into the
request path. Helidon's own instrumentation is a dependency we can see and pin, maintained by the Helidon team, and
released with Helidon. The agent's module is community-maintained and trails Helidon releases (Helidon 27 support is
still an open PR).

### Why disable library instrumentation?
- **Explicit**: spans exist only where we chose them; nothing appears or vanishes when a library is upgraded and the
  agent's version checks silently skip it.
- **No duplicates**: the agent can't add a second server span next to Helidon's.
- **Cheaper**: less startup time spent rewriting classes, less span volume and backend cost.
- **Reversible**: enabling JDBC tracing later is one flag (`otel.instrumentation.jdbc.enabled=true`), not a code change.

### Why a single SDK?
- **One pipeline**: Helidon, annotation and manual spans share one exporter, sampler and `Resource`, configured once.
- **Ready for logs and metrics**: the same SDK can export them later.

### Why build Helidon's `Tracer` explicitly?
- **Decoupled**: Helidon reuses the agent's SDK only when told to; dropping the agent means flipping
  `otel.agent.present` and adding a `tracing:` node, with no code change.
- **No hidden globals**: replaces the deprecated `Tracer.global()` (removal planned since 4.4.0) and Helidon's service
  registry lookup; the `Tracer` comes from our `Config` through our registry.

### Why a config file for the agent?
The agent's settings live in the repo next to `application.yaml`, under review like the rest of the config, instead of
being scattered across env vars and deployment scripts.

### Why standard OTel API only?
Our code imports `io.opentelemetry.*`, not Helidon or vendor types. Dropping Helidon's tracing or switching backends
doesn't touch the annotated code.

---

## 5. Alternatives Considered

| Option | Rejected because |
| :--- | :--- |
| **Full agent auto-instrumentation** | Implicit span set, duplicate server spans with Helidon, higher startup and volume; still no spans for our own methods. |
| **Agent's Helidon module for server spans** | Puts agent bytecode rewriting in the request path; community-maintained and trails Helidon releases. |
| **Helidon only, hand-written spans** | Tracing code in business logic; Helidon's span builder has no parent by default (`setNoParent()`), so a missed `.parent(...)` starts a separate trace. |
| **Helidon `@Tracing.Traced`** | Requires Helidon declarative codegen, which our KSP endpoints don't use. |
| **App-owned SDK used by the agent** | Not supported: the agent always runs its own SDK ([interop design](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/misc/interop-design/interop-design.md); [#12381](https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/12381) closed as not planned). |
| **Separate SDKs for Helidon and the agent** | Two exporters and two configs to keep in sync; breaks the single-pipeline goal. |
| **`Services.get(Tracer)` / no `tracing:` node** | Falls back to `GlobalOpenTelemetry` implicitly: without the agent, server spans silently go to a no-op SDK. |

---

## 6. Trade-offs

- **Agent required for annotated spans**: the jar must ship in the image (not yet done); `:app:run` attaches it today.
- **Forced flag hides a missing agent**: with `otel.agent.present: true` and no agent (IDE run of `MainKt`, tests,
  e2e image), `GlobalOpenTelemetry.get()` installs a no-op SDK for good. Tracing stops, with no error.
- **Fallback not configured yet**: there's no `tracing:` node, so with the flag off Helidon exports with its defaults
  (gRPC to `localhost:4317`), not to VictoriaTraces.
- **Two config files**: `application.yaml` for Helidon, `otel.properties` for the agent's SDK. Only one SDK is active,
  but service name and endpoint live in different files depending on the mode.
- **No GraalVM native image** while the agent is in use: agents need a JVM.

---

## 7. Open Questions

### 7.1 Helidon's span conventions
Observed with 4.5.5:
- Server spans are named `HTTP Request`, with no method or route.
- They use old attribute names (`http.status_code`, `http.url`, `http.method`).
- A `404` marks the server span as an error (OTel says 4xx shouldn't, for server spans).
- Each request also emits an extra `content-write` span.

`helidon-webserver-observe-telemetry-tracing` switches to current names and adds `http.route`; the span name becomes the
method only (`GET`).

---

### 7.2 Remaining plumbing
- Agent jar and `otel.properties` in the container image.
- Local backend: VictoriaTraces plus Grafana in a compose file.

---

## 8. Verification

Agent 2.32.0, Helidon 4.5.5, `:app:run` exporting to VictoriaTraces v0.12.1:
- Helidon's server spans (scope `io.helidon.tracing`) and `@WithSpan` spans arrive under service `stopgap` through the
  agent's SDK.
- `@WithSpan` spans share the request's trace id and have the server span as parent.
- An incoming `traceparent` is continued.
- An exception in a `@WithSpan` method sets `ERROR` status and records an `exception` event.
