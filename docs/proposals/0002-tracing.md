# RFC-0002: Distributed Tracing

- **Date**: 2026-10-23
- **Target**: `stopgap` / `app`
- **Scope**: Traces only (metrics, logs and profiles are separate proposals)

---

## 1. Summary

Helidon traces the HTTP edge with its own instrumentation. The OpenTelemetry (OTel) Java agent runs with library
instrumentation disabled and only turns `@WithSpan` annotations on our code into spans. Both write to one SDK, the
agent's, and share one `Context`, so a request yields one trace from the server span down to our business methods.

The guiding principle is **thin, decoupled layers**: each layer does one job, with as little hidden behavior as
possible. Removing the agent should leave the Helidon setup nearly untouched; we'd lose only the annotated spans.

---

## 2. Goals & Non-Goals

### Goals
- One server span per request, continuing an incoming `traceparent`.
- Spans for chosen business methods, declared with annotations rather than tracing code.
- A single SDK and exporter for all spans.
- Application code depends only on the standard OTel API and annotations.

### Non-Goals
- **Library auto-instrumentation** (JDBC, HTTP clients, Helidon's server): each is a deliberate later opt-in.
- **Span per method**: method-level cost belongs to profiling.
- **Metrics, logs, profiles**: separate proposals.

---

## 3. Design

```
                    single OTel SDK (agent's)
                     ▲                      ▲
Helidon tracing                      @WithSpan on our methods
(server span per request)            (agent: annotations only)
     └──────────────── same Context per request ────────────────┘
```

- **Helidon** creates server spans. Its `Tracer` is built explicitly: on the agent's SDK when the agent is attached,
  otherwise on its own SDK from Helidon config.
- **The agent** has every instrumentation off except annotation support and the bridges it depends on. It's attached
  only in the container image and configured from a file in the repo.
- **Our code** adds attributes to the current span first, and a new span only for a unit with its own latency or
  failure mode, or one that fans out.

---

## 4. Why This Approach

### Why thin layers?
Each layer we add is behavior we can't see in our code. With the agent's role kept narrow, server tracing is plain
Helidon code we wire ourselves, and dropping the agent costs only the annotated spans.

### Why annotations?
Tracing is a cross-cutting concern. An annotation states the intent and keeps span handling out of business code.

### Why the agent for annotations?
- **No proxies**: it rewrites the method's own bytecode, so self-invocation and Kotlin's final classes are traced.
  Proxy-based AOP misses both.
- **Inert without it**: unit tests and local runs are unaffected.
- **Helidon's `@Tracing.Traced` doesn't fit**: it needs Helidon's codegen, and our endpoints use our own KSP codegen.

### Why Helidon for the server span?
The agent has a Helidon module, but it would put bytecode rewriting in the request path. Helidon's instrumentation is
a visible, pinned dependency, maintained by the Helidon team and released with Helidon. The agent's module is
community-maintained and trails Helidon releases.

### Why disable library instrumentation?
- **Explicit**: spans exist only where we chose them; nothing silently appears or vanishes on a library upgrade.
- **No duplicates**: no second server span next to Helidon's.
- **Cheaper**: less startup work and span volume.
- **Reversible**: any library can be enabled later with one flag.

### Why a single SDK, owned by the agent?
- **One pipeline**: one exporter, sampler and `Resource` for every span.
- **No choice**: the agent always runs its own SDK and can't adopt one built by the app.

### Why build Helidon's `Tracer` explicitly?
- **Decoupled**: Helidon uses the agent's SDK only when told to; without the agent it falls back to its own config.
- **No hidden globals**: replaces the deprecated `Tracer.global()` and Helidon's service-registry lookup.

### Why standard OTel API only?
Dropping Helidon's tracing or switching backends doesn't touch annotated code.

---

## 5. Alternatives Considered

| Option | Rejected because |
| :--- | :--- |
| **Full agent auto-instrumentation** | Implicit span set, duplicate server spans, higher startup and volume; still no spans for our own methods. |
| **Agent's Helidon module** | Bytecode rewriting in the request path; trails Helidon releases. |
| **Hand-written spans** | Tracing code in business logic; Helidon's span builder has no parent by default, so a missed `.parent(...)` starts a separate trace. |
| **App-owned SDK used by the agent** | Not supported by the agent ([interop design](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/misc/interop-design/interop-design.md), [#12381](https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/12381)). |
| **Separate SDKs for Helidon and the agent** | Two exporters and configs to keep in sync. |
| **Implicit global SDK lookup** | Without the agent, server spans silently go to a no-op SDK. |

---

## 6. Trade-offs

- **Spans only where the agent runs**: the image, not local runs or tests.
- **A missing agent is silent**: Helidon is told the agent is present rather than detecting it, so without it tracing
  stops with no error.
- **Two config files**: Helidon's and the agent's; only one SDK is active, but its settings depend on the mode.
- **No GraalVM native image** while the agent is in use.

---

## 7. Open Questions

- **Helidon's span conventions**: server spans are named `HTTP Request`, use old attribute names, mark `404` as an
  error, and come with an extra `content-write` span. Helidon's telemetry-tracing module moves to current conventions
  and adds `http.route`, though the span name becomes only the method.
- **Local backend**: VictoriaTraces plus Grafana.
