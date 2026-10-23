# RFC-0002: Distributed Tracing and Logs

- **Date**: 2026-10-23
- **Target**: `stopgap` / `app`
- **Scope**: Traces and logs (metrics and profiles are separate proposals)

---

## 1. Summary

Helidon traces the HTTP edge, the OpenTelemetry (OTel) JDBC library traces database statements, and the OTel Java agent
only turns `@WithSpan` annotations on our code into spans. All three write to one SDK, the agent's, and share one
`Context`, so a request yields one trace from the server span through our business methods down to each query.
Logs go through the same SDK via the OTel Logback appender, so each record carries the trace and span it was written
in.

The guiding principle is **thin, decoupled layers with explicit control**: each layer does one job, is wired in our code,
and receives the SDK from us. Nothing builds or discovers an SDK on its own.

---

## 2. Goals & Non-Goals

### Goals
- One server span per request, continuing an incoming `traceparent`.
- Spans for chosen business methods, declared with annotations rather than tracing code.
- A span per database statement, for the store's queries only.
- A single SDK and exporter for all spans, handed explicitly to every layer.
- Application code depends only on the standard OTel API and annotations.
- Every log record exported through the same SDK, correlated with the active span.

### Non-Goals
- **Agent library instrumentation** (JDBC, HTTP clients, Helidon's server): each layer is wired explicitly instead.
- **Connection pool (HikariCP)**: it produces metrics, not spans; it belongs to the metrics proposal.
- **Span per method**: method-level cost belongs to profiling.
- **Agent log instrumentation**: the appender is wired explicitly instead.
- **Metrics, profiles**: separate proposals.

---

## 3. Design

```
                              single OTel SDK (agent's)
                ▲                   ▲                     ▲                    ▲
Helidon tracing           @WithSpan on our methods     JDBC library         Logback appender
(server span)             (agent: annotations only)    (store's DataSource) (every log record)
     └──────────────────────────── same Context per request ───────────────────────┘
```

- **One `OpenTelemetry`** comes from our registry: the agent's SDK when the agent is attached, otherwise an explicit
  no-op. Helidon's tracer and the JDBC wrapper both receive it.
- **Helidon** creates server spans with a `Tracer` we build on that `OpenTelemetry`.
- **JDBC** spans come from wrapping the store's `DataSource`; health checks use the raw pool and stay untraced.
- **Logs** go through an `OpenTelemetryAppender` in `logback.xml`, next to the console appender. `main` installs that
  `OpenTelemetry` into it; records logged before that are buffered and exported once it's installed.
- **Access log** runs inside the server span (weight 875, below tracing's 900) so each line carries its trace id;
  Helidon's default of 1000 writes it after the span has closed.
- **The agent** has every instrumentation off except annotation support and the bridges it depends on. It's attached
  only in the container image and configured from a file in the repo.
- **Our code** adds attributes to the current span first, and a new span only for a unit with its own latency or
  failure mode, or one that fans out.

---

## 4. Why This Approach

### Why thin layers with explicit control?
Each layer we add is behavior we can't see in our code. When every layer is wired by us and handed its SDK, the
whole tracing setup can be read from our config classes, and no library creates telemetry we didn't ask for.

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

### Why the JDBC library, not the agent's JDBC instrumentation?
Both run the same instrumentation code and produce the same spans. The library traces only the `DataSource` we wrap,
so we choose what's traced; the agent rewrites the driver and traces every connection, including pool housekeeping and
health checks.

### Why the Logback appender library, not the agent's log instrumentation?
Same reasoning as JDBC: the appender is a line in `logback.xml` and an `install` call we can read, and it keeps
exporting if the agent is replaced. The agent would hook Logback through bytecode rewriting, and both together would
export every record twice.

### Why disable agent library instrumentation?
- **Explicit**: spans exist only where we chose them; nothing silently appears or vanishes on a library upgrade.
- **No duplicates**: running the agent's JDBC instrumentation next to the library records every statement twice.
- **Cheaper**: less startup work and span volume.

### Why a single SDK, owned by the agent?
- **One pipeline**: one exporter, sampler and `Resource` for every span and log record.
- **No choice**: the agent always runs its own SDK and can't adopt one built by the app.

### Why a no-op without the agent?
Helidon could build its own SDK from its config, but that's an SDK nobody hands it. Falling back to an explicit no-op
keeps every SDK decision in our code; tracing without the agent would be a deliberate SDK we build ourselves.

### Why standard OTel API only?
Dropping Helidon's tracing or switching backends doesn't touch annotated code.

---

## 5. Alternatives Considered

| Option | Rejected because |
| :--- | :--- |
| **Full agent auto-instrumentation** | Implicit span set, duplicate server spans, higher startup and volume; still no spans for our own methods. |
| **Agent's Helidon module** | Bytecode rewriting in the request path; trails Helidon releases. |
| **Agent's JDBC instrumentation** | Traces every connection implicitly; duplicates the library's spans if both run. |
| **Hand-written spans** | Tracing code in business logic; Helidon's span builder has no parent by default, so a missed `.parent(...)` starts a separate trace. |
| **App-owned SDK used by the agent** | Not supported by the agent ([interop design](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/misc/interop-design/interop-design.md), [#12381](https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/12381)). |
| **Helidon building its own SDK** | An SDK we don't control; a second SDK whenever the agent is attached. |
| **Agent's Logback instrumentation** | Hooks logging implicitly; duplicates the appender's records if both run. |
| **App-built SDK for logs** | A second SDK next to the agent's, with its own `Resource` and exporter config. |

---

## 6. Trade-offs

- **No agent, no traces or exported logs**: local runs and tests use a no-op SDK; logs still reach the console.
- **A missing agent is silent**: Helidon is told the agent is present rather than detecting it, so without it tracing
  stops with no error.
- **Settings in two places**: the agent reads `otel.properties`, but the JDBC library reads only system properties, so
  its options (e.g. the semconv opt-in) go on the command line.
- **Alpha libraries**: the JDBC library and the Logback appender are versioned `-alpha`; their APIs may change
  between releases.
- **No GraalVM native image** while the agent is in use.

---

## 7. Open Questions

- **Helidon's span conventions**: server spans are named `HTTP Request`, use old attribute names, mark `404` as an
  error, and come with an extra `content-write` span. Helidon's telemetry-tracing module moves to current conventions
  and adds `http.route`, though the span name becomes only the method.
- **Startup queries**: statements run before any request (e.g. table creation) become standalone traces.
- **Local backend**: Grafana LGTM (Tempo for traces, Loki for logs).
