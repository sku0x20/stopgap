# RFC-0002: Observability (Traces, Logs, Metrics and Profiles)

- **Date**: 2026-10-23
- **Target**: `stopgap` / `app`
- **Scope**: Traces, logs, metrics and profiles

---

## 1. Summary

Helidon traces the HTTP edge, the OpenTelemetry (OTel) JDBC library traces database statements, and the OTel Java agent
only turns `@WithSpan` annotations on our code into spans. Helidon's WebClient traces outbound HTTP calls. All of them
write to one SDK, the agent's, and share one `Context`, so a request yields one trace from the server span through our
business methods down to each query and outbound call.
Logs go through the same SDK via the OTel Logback appender, so each record carries the trace and span it was written
in.

Metrics go through it too. Helidon records HTTP server and client durations with the OTel API, OTel's Micrometer bridge
carries Helidon's own Micrometer meters, OTel libraries add the connection pool and the JVM, and our code records
business metrics with the OTel `Meter` API. The agent exports them over OTLP; there's no scrape endpoint.

Profiles come from Pyroscope's extension to the agent: it runs async-profiler in the process and tags each request's
samples with its server span, so a slow span leads to the code that was running. A rolling JFR recording keeps the last
ten minutes of JVM events to dump on demand.

The guiding principle is **thin, decoupled layers with explicit control**: each layer does one job, is wired in our code,
and receives the SDK from us. Nothing builds or discovers an SDK on its own.

---

## 2. Goals & Non-Goals

### Goals
- One server span per request, continuing an incoming `traceparent`.
- Spans for chosen business methods, declared with annotations rather than tracing code.
- A span per database statement, for the store's queries only.
- A span per outbound HTTP call, propagating the trace to the callee.
- A single SDK and exporter for all spans, handed explicitly to every layer.
- Application code depends only on the standard OTel API and annotations.
- Every log record exported through the same SDK, correlated with the active span.
- Request duration per route and per outbound call, under OTel semantic-convention names.
- Connection pool, database operation and JVM metrics.
- Business metrics recorded with the OTel `Meter` API, exported through the same SDK.
- Continuous CPU, allocation and lock profiles, with each server span linked to the samples taken while it ran.
- A rolling JFR recording of JVM events (GC, locks, I/O, virtual-thread pinning) to dump when something looks wrong.

### Non-Goals
- **Agent library instrumentation** (JDBC, HTTP clients, Helidon's server, Micrometer, JVM runtime): each layer is wired
  explicitly instead.
- **Span per method**: method-level cost belongs to profiling.
- **Agent log instrumentation**: the appender is wired explicitly instead.
- **Metrics annotations** (`@Timed`, `@Counted`): OTel has none; business metrics use the `Meter` API.
- **Prometheus scrape endpoint**: metrics leave only over OTLP.
- **OTLP profiles**: OTel has no in-process profiling for Java yet; only the out-of-process eBPF profiler exports
  OTLP profiles, and it's alpha.

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
- **Outbound HTTP** spans come from Helidon's `WebClientTracing`, built on the same `Tracer` and added to each
  `WebClient` with service discovery off; discovery would add a second one on `Tracer.global()`. It injects
  `traceparent` into the request. A `LoggingClientService` inside it writes one line per call with status and duration.
- **Logs** go through an `OpenTelemetryAppender` in `logback.xml`, next to the console appender. `main` installs that
  `OpenTelemetry` into it; records logged before that are buffered and exported once it's installed.
- **Access log** runs inside the server span (weight 875, below tracing's 900) so each line carries its trace id;
  Helidon's default of 1000 writes it after the span has closed.
- **The agent** has every instrumentation off except annotation support and the bridges it depends on. It's attached
  only in the container image and configured from a file in the repo.
- **Our code** adds attributes to the current span first, and a new span only for a unit with its own latency or
  failure mode, or one that fans out.

### Metrics

```
                              single OTel SDK (agent's)
         ▲                       ▲                        ▲                       ▲
Helidon OTel metrics      Micrometer bridge         OTel libraries            Meter API
(server and client        (Helidon's request KPIs   (JDBC, HikariCP,          (our business
 HTTP duration)            and system meters)        JVM runtime)              metrics)
```

- **Helidon's registry** gets our `OpenTelemetry` too: Helidon's metrics modules look it up in Helidon's `Services`
  rather than taking it as a parameter, so `main` binds it there with `Services.set` before building anything Helidon.
- **Server duration** (`http.server.request.duration`, by `http.route`) comes from the filter Helidon's
  telemetry-metrics module adds to `MetricsObserver`, which joins the observers in our config.
- **Client duration** (`http.client.request.duration`) comes from Helidon's `WebClientTelemetryMetrics`, added to the
  httpbin `WebClient` next to `WebClientTracing`.
- **Helidon's Micrometer meters** (request KPIs, system and virtual-thread meters) live on Micrometer's global registry.
  `main` adds OTel's `OpenTelemetryMeterRegistry` to it, so they reach the same SDK. Its Prometheus publisher is off, so
  there's no other registry and no `/observe/metrics`.
- **Connection pool** metrics (`db.client.connection.*`) come from OTel's HikariCP library, set as the pool's metrics
  tracker. **Database operation** duration (`db.client.operation.duration`) comes from the JDBC library already wrapping
  the store's `DataSource`.
- **JVM** metrics (`jvm.*`) come from OTel's runtime-telemetry library, started in `main` and closed on shutdown.
- **Business metrics** use the OTel `Meter` API in a small class per area (e.g. `NoteMetrics`), created in our registry
  from the same `OpenTelemetry`. Instruments are built once; attribute values stay a small fixed set, since each
  distinct combination is its own time series.
- **The agent** exports metrics over OTLP every 60s (2s in e2e).

### Profiles

```
OTel agent ── Pyroscope extension ──┬── span processor on the agent's SDK: tags the root span with pyroscope.profile.id
                                    │   and the profiler's thread context with its span id
                                    └── async-profiler (CPU via ctimer, alloc, lock) ── JFR every 10s ──▶ Pyroscope
JVM ── JFR recording (last 10 min, in the container) ──▶ jcmd JFR.dump on demand
```

- **The extension** is loaded into the agent with `otel.javaagent.extensions`, so there's still one agent and one SDK.
  It adds a span processor to that SDK and starts the profiler it bundles. It comes from GitHub releases, not Maven
  Central, through an Ivy repository limited to that one module.
- **Span linking**: when a root span starts (Helidon's server span), the processor sets the span id as the profiler's
  context on the current thread and adds `pyroscope.profile.id` to the span; it clears the context when the span ends.
  Samples taken in between carry the span id, nested spans included.
- **Profiler settings** live in `pyroscope.properties`, next to `otel.properties`: the profiler reads only its own file,
  environment and system properties. `pyroscope.application.name` must match `otel.service.name`, since Grafana finds a
  span's profile by service name; the default is a random `javaspy.*` name.
- **CPU via `ctimer`**: async-profiler's `cpu` event needs `perf_events`, which containers usually block. `ctimer` is a
  CPU-time timer per thread: it needs no permissions but records no kernel frames. `itimer`, the default, is one timer
  for the whole process, so it takes at most one sample per tick however many threads are busy.
- **Allocation and lock** samples ride in the same upload: one per 512 KiB allocated (`pyroscope.profiler.alloc`), and
  one per lock wait of 10ms or more (`pyroscope.profiler.lock`).
- **The JFR recording** starts with the JVM (`-XX:StartFlightRecording`, default settings, `maxage=10m`) and stays in
  the container; `jcmd 1 JFR.dump name=continuous filename=<file>` copies it out for JDK Mission Control or IntelliJ.

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

### Why Helidon's WebClient tracing for outbound calls?
Same reason as the server span: it's Helidon's own, takes the `Tracer` we hand it, and is added per client in our
config. The OTel `java-http-client` library would mean a second client stack and another `-alpha` dependency.

### Why the JDBC library, not the agent's JDBC instrumentation?
Both run the same instrumentation code and produce the same spans. The library traces only the `DataSource` we wrap,
so we choose what's traced; the agent rewrites the driver and traces every connection, including pool housekeeping and
health checks.

### Why the Logback appender library, not the agent's log instrumentation?
Same reasoning as JDBC: the appender is a line in `logback.xml` and an `install` call we can read, and it keeps
exporting if the agent is replaced. The agent would hook Logback through bytecode rewriting, and both together would
export every record twice.

### Why Helidon's metrics modules for HTTP durations?
Same reason as Helidon's tracing: they're Helidon's own, record semantic-convention names, and take the route from the
pattern that matched. Unlike tracing they don't take the SDK as a parameter; they look it up in Helidon's `Services`.
Binding it there with `Services.set` keeps the hand-off in our code, and it throws if anything looked the instance up
first, so a wrong order fails at startup instead of recording into another SDK.

### Why OTel's Micrometer bridge?
Helidon's `MetricsObserver` is built on Micrometer, and its request KPIs and system meters are Micrometer meters. The
bridge is a Micrometer registry that writes to the `OpenTelemetry` we hand it, the same shape as the JDBC wrapper.
Micrometer's own OTLP registry would be a second exporter and `Resource`; Helidon's Prometheus publisher a second,
pull-based path.

### Why OTel libraries for the pool and the JVM?
Same reasoning as JDBC: each takes our `OpenTelemetry`, and they record semantic-convention names
(`db.client.connection.*`, `jvm.*`). Helidon's system meters stay: their MicroProfile-style names (`memory.usedHeap`,
`thread.count`) don't collide with `jvm.*`, and its virtual-thread meters (`vthreads.*`, pinning) have no stable OTel
counterpart.

### Why the `Meter` API, not annotations, for business metrics?
OTel has no metrics annotation
([#7030](https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/7030), open since 2022). Micrometer's `@Timed` and `@Counted` are AspectJ aspects: without Spring they need AspectJ weaving, a
second bytecode rewriter next to the agent, and they put Micrometer's API in our code. A business metric is a
deliberate name and attribute set, so explicit code fits it; method timing already comes from `@WithSpan` spans.

### Why the agent's Pyroscope extension?
- **One agent**: Pyroscope's own `-javaagent` would be a second bytecode agent; the extension runs inside ours and
  hooks into its SDK, the only place a span processor can go.
- **Span links**: tagging samples with span ids needs a hook on span start and end, which the extension provides.
- **Same shape as the agent**: it's configured from a file in the repo and attached only in the image. Starting the
  profiler from our code instead would still need a span processor on the agent's SDK, which only an extension can add.

### Why async-profiler, not JFR's sampler?
Pyroscope already uploads JFR files; the question is which sampler fills them. JFR's execution sampler takes a few
running Java threads per tick, stops at the Java boundary, and can't attach labels to samples. async-profiler records
mixed Java and native stacks for every thread and keeps a per-thread context id, which is how samples carry span ids.

### Why a JFR recording as well?
JFR's value is its events, not its samples: GC pauses, JIT, safepoints, socket and file I/O, monitor waits, and
`jdk.VirtualThreadPinned`, which flags a virtual thread stuck on its carrier. Pinning starves Helidon's carriers under
load without showing in a flame graph. The default settings cost about 1%, and the JVM records it with no dependency.

### Why disable agent library instrumentation?
- **Explicit**: spans exist only where we chose them; nothing silently appears or vanishes on a library upgrade.
- **No duplicates**: running the agent's JDBC instrumentation next to the library records every statement twice.
- **Cheaper**: less startup work and span volume.

### Why a single SDK, owned by the agent?
- **One pipeline**: one exporter, sampler and `Resource` for every span, log record and metric.
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
| **Agent's HTTP client instrumentation** | Traces every client implicitly; duplicates `WebClientTracing`'s spans if both run. |
| **Agent's JDBC instrumentation** | Traces every connection implicitly; duplicates the library's spans if both run. |
| **Hand-written spans** | Tracing code in business logic; Helidon's span builder has no parent by default, so a missed `.parent(...)` starts a separate trace. |
| **App-owned SDK used by the agent** | Not supported by the agent ([interop design](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/misc/interop-design/interop-design.md), [#12381](https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/12381)). |
| **Helidon building its own SDK** | An SDK we don't control; a second SDK whenever the agent is attached. |
| **Agent's Logback instrumentation** | Hooks logging implicitly; duplicates the appender's records if both run. |
| **App-built SDK for logs** | A second SDK next to the agent's, with its own `Resource` and exporter config. |
| **Prometheus scrape endpoint** | A second, pull-based path out, with its own names and labels. |
| **Micrometer's OTLP registry** | A second exporter and `Resource` next to the agent's. |
| **Agent's Micrometer and runtime modules** | Hook the global registry and the JVM implicitly; the libraries take our instance. |
| **Own HTTP metrics filter** | Re-implements Helidon's semantic-convention code, which we'd then maintain. |
| **Micrometer `@Timed`/`@Counted`** | AspectJ weaving next to the agent, and Micrometer's API in business code. |
| **HikariCP's Micrometer tracker** | Hikari's own names instead of `db.client.connection.*`. |
| **Pyroscope's `-javaagent`** | A second bytecode agent; it still needs the extension for span links. |
| **Pyroscope as a library, started in `main`** | Still needs the extension's span processor on the agent's SDK, so two places to wire one profiler. |
| **OTel eBPF profiler** | Out of process, needs root and Linux; alpha; no span links; profiles every process on the host. |
| **JFR's sampler (`PYROSCOPE_PROFILER_TYPE=JFR`)** | Few threads per tick, Java frames only, no labels for span links. |
| **Inferred spans (OTel contrib)** | Turns samples into spans, against the "Span per method" non-goal. |

---

## 6. Trade-offs

- **No agent, no traces, metrics, profiles or exported logs**: local runs and tests use a no-op SDK; logs still reach
  the console.
- **A missing agent is silent**: Helidon is told the agent is present rather than detecting it, so without it tracing
  stops with no error.
- **Settings in three places**: the agent reads `otel.properties` and the profiler `pyroscope.properties`, but the JDBC
  library reads only system properties, so its options (e.g. the semconv opt-in) go on the command line.
- **Alpha libraries**: the JDBC, HikariCP, Micrometer bridge and runtime-telemetry libraries and the Logback appender
  are versioned `-alpha`; their APIs may change between releases.
- **No GraalVM native image** while the agent is in use.
- **Client spans end at the response headers**: `WebClientTracing` ends the span once the status arrives, so reading
  the body isn't counted; the logged duration matches the span.
- **Client span re-activated by hand**: `WebClientTracing` keeps its span in Helidon's context and never makes it
  current in OTel's, which the Logback appender reads. `LoggingClientService` re-activates it from its ids
  ([helidon#8861](https://github.com/helidon-io/helidon/issues/8861) workaround) for the duration of the call, so
  everything logged inside it (later client services, Helidon's connection code) carries the client span. Clients
  without it, and code after the call returns, see the server span
  ([helidon#5216](https://github.com/helidon-io/helidon/issues/5216)).
- **`Services.set` ordering**: it must run in `main` before anything Helidon is built; the first lookup fixes the
  instance and a later `set` throws.
- **A `telemetry` config node**: Helidon's metrics modules require `telemetry.service` for their meter scope name.
  `enabled: false` stops them from building an SDK or setting the global one. The node also changes what Helidon's
  registry serves to code not covered by `Services.set` (e.g. intTests), where `Tracer.global()` then throws; test
  clients turn service discovery off so a discovered `WebClientTracing` never calls it.
- **Correct server durations are opt-in**: without `use-updated-http-metrics: true`, Helidon 4.x records milliseconds
  into a histogram declared in seconds ([helidon#12097](https://github.com/helidon-io/helidon/issues/12097)).
- **Client metric attributes**: `WebClientTelemetryMetrics` names the method `http.client.request.method` (semantic
  conventions: `http.request.method`) and uses the raw path as `url.template`, so each distinct path is its own series.
  Fine for httpbin's bounded paths, not for paths carrying ids.
- **The bridge comes first**: a Micrometer registry added after a meter exists misses that meter's earlier recordings,
  so `main` adds it before building anything Helidon.
- **Two sets of JVM metrics**: Helidon's system meters overlap `jvm.*` under other names.
- **No active Micrometer publisher**: with Prometheus off, Helidon logs a warning that none is configured (per its
  source; not yet seen in a startup log).
- **Helidon metric quirks, left as is**: the server metric sets `error.type` to the status text for every status but
  `200` (e.g. `Created` for `201`), where the conventions set it only for errors; and Helidon's `requests.count` KPI,
  exported through the bridge, repeats the count in `http.server.request.duration`.
- **Profiles leave over Pyroscope's protocol**, not OTLP: a second path out, next to the agent's exporter. OTLP profiles
  from inside the JVM aren't possible yet.
- **Slower startup**: the extension adds about 190ms (≈1265 to 1460ms in the image), the JFR recording about 35ms.
- **Outside Maven Central**: the extension is downloaded from GitHub releases; it bundles its own async-profiler, OkHttp
  and Kotlin stdlib, shaded into the agent's extension class loader.
- **Root spans only**: samples are tagged with the server span's id, so a child span's own profile can't be told apart.
  Spans shorter than the 10ms sampling interval may have no samples.
- **Native access warning**: async-profiler loads its library through `System.load`, which JDK 25 warns about without
  `--enable-native-access` and a later release will block.
- **The JFR recording dies with the container**: dump it before stopping the container; nothing ships it anywhere.

---

## 7. Open Questions

- **Helidon's span conventions**: server spans are named `HTTP Request`, use old attribute names, mark `404` as an
  error, and come with an extra `content-write` span. Helidon's telemetry-tracing module moves to current conventions
  and adds `http.route`, though the span name becomes only the method. Client spans have the same issue: named
  `GET-<url>` with `http.url`/`http.status_code`.
- **Startup queries**: statements run before any request (e.g. table creation) become standalone traces.
- **Span links on virtual threads**: the processor sets the profiler's context on the current thread when the span
  starts. Helidon runs each request on a virtual thread, which can park and resume on another carrier; if the context
  sticks to the carrier, samples may land on the wrong span or none. Not yet checked.
- **Traces-to-profiles in Grafana**: LGTM's Tempo data source doesn't set `tracesToProfiles`, so the "profiles for this
  span" link has to be configured by hand.
- **Local backend**: Grafana LGTM (Tempo for traces, Loki for logs, Prometheus for metrics, Pyroscope for profiles).
