# RFC-0003: Single Serde per Endpoint

- **Date**: 2026-10-25
- **Target**: `stopgap` / `helidon-extensions`
- **Scope**: Request and response body (de)serialization, content negotiation

---

## 1. Summary

Each endpoint method has exactly one serde, chosen at build time. It reads the request body and writes the response
body. There is no runtime content negotiation: the request's `Content-Type` is only checked against the serde, and
`Accept` is ignored.

The guiding principle is **no runtime magic**: reading an endpoint tells you exactly which format it speaks, and the
generated code calls that serde directly.

---

## 2. Motivation

The first redesign (#46) aimed for full RFC 9110 negotiation: a catalog picks a deserializer by `Content-Type` and a
serializer by `Accept`. Building it on the `serde-api-redesign` branch kept surfacing more machinery than value:

- **Matching policy grew without end.** Case-insensitivity, charset aliases, JSON's meaningless `charset`, absent
  charsets, extra parameters, then `Accept` q-values, wildcards, specificity, `q=0` and `Vary: Accept`.
- **Declared media types don't scale.** A serde listing the exact types it accepts breaks on parameters whose value
  changes per request, like multipart's `boundary`. Only a predicate (`canDeserialize`) works, which makes lookup a scan.
- **A lookup cache needs abuse defences.** Its key is the client-chosen header text, so an attacker can grow it without
  bound by varying parameters. Bounding it means capacity limits, admission filters or LFU eviction.
- **Nobody needs it.** APIs speak one format per operation. Stripe and GitHub send JSON whatever `Accept` says.

---

## 3. Goals & Non-Goals

### Goals
- One serde per endpoint method, declared at build time with a project-wide default and an annotation override.
- The generated code calls the serde directly: no catalog, scan or cache at runtime.
- The request's `Content-Type` is checked against the serde: `415` when it's missing or not accepted.
- The response's `Content-Type` is always the serde's media type.

### Non-Goals
- **Proactive negotiation** (`Accept`): ignored, so no `406` and no `Vary: Accept`.
- **Several formats on one endpoint**, e.g. JSON and form data on the same route: use two endpoints.
- **Different serdes for request and response** on one endpoint.
- **Multipart** (#54): needs a per-request `boundary` and streaming; designed separately.

---

## 4. API Specification

```kotlin
/** Reads and writes bodies in one format, e.g. UTF-8 JSON. */
interface Serde : Serializer, Deserializer
```

`Deserializer.canDeserialize(mediaType)` owns all matching rules for that format (case, `charset`). `Serializer.mediaType`
is the response `Content-Type`.

```kotlin
@Endpoint("/notes")
class NoteEndpoint // uses the default serde

@Endpoint("/legacy")
@CustomSerde(clazz = PlainTextSerde::class) // whole class
class LegacyEndpoint {

    @Get("/json")
    @CustomSerde(qualifier = SerdeExtras.DEFAULT_SERDE_QUALIFIER) // one method
    fun getJson(): ResDto
}
```

`@CustomSerde` replaces `@CustomSerdeCatalog` and resolves the serde through the same DI qualifiers. The innermost
declaration wins: method, then class, then the default.

---

## 5. Detailed Design

### 5.1 Request

When the endpoint takes a body parameter, the generated code:

1. reads `Content-Type`, throwing `MissingContentTypeException` (`415`) when it's absent;
2. throws `UnsupportedMediaTypeException` (`415`) unless `serde.canDeserialize(contentType)`;
3. calls `serde.deserialize(bytes, type)` with a cached `KType`; malformed bodies are `400`.

### 5.2 Response

When the endpoint returns a value, the generated code sets `Content-Type` to `serde.mediaType` and sends
`serde.serialize(value)`. `Accept` is never read.

### 5.3 Status Codes

| Case | Status |
|---|---|
| `Content-Type` missing | `415` (`MissingContentTypeException`) |
| `Content-Type` not accepted by the serde | `415` (`UnsupportedMediaTypeException`) |
| Body doesn't parse | `400` |
| `Accept` doesn't include the serde's type | ignored, `200` |

A missing `Content-Type` is `415`, not `400`: RFC 9110 §8.3 lets the recipient treat the body as
`application/octet-stream`, which no serde accepts.

---

## 6. Design Rationale & Alternatives Considered

### Why ignoring `Accept` is allowed
RFC 9110 §12.5.1 lets a server either honor `Accept` with a `406` or disregard it and send its default
representation. §12.1 itself lists proactive negotiation's costs: the server can't know what's best for the user, it
complicates the origin server, and it limits shared-cache reuse.

### Why it fits an API server
An API's contract is its schema, and an OpenAPI operation documents one request and one response media type. Clients
code against that one format. Negotiation adds cases nobody tests: a browser sending `Accept: text/html` would get a
`406` instead of readable JSON.

### Why it fits the implementation
The choice is made at build time, so the generated code has nothing to look up. No matching policy, no scan, no cache,
no abuse surface. A wrong or missing qualifier fails where today's catalog qualifiers fail, not on the first request.

### Full negotiation (rejected)
The branch's `MediaTypeSerdeCatalog`: a `canDeserialize` scan for `Content-Type`, `bestAccepted` for `Accept`. Correct
but costly for the reasons in §2, and it still needed a bounded cache with admission control.

### Declared media type lists (rejected)
`Deserializer.mediaTypes` with an exact-match map. Fast, but per-request parameters (`boundary`) can't be listed, and
the catalog has to normalize keys, which is matching policy again.

### Ignoring `Content-Type` too (rejected)
Simplest, but unsafe; see §7.

---

## 7. Security Considerations

### Cross-site request forgery via simple requests
A browser sends a cross-site `POST` without a CORS preflight when its `Content-Type` is `text/plain`,
`application/x-www-form-urlencoded` or `multipart/form-data`. If a JSON endpoint parsed any body regardless of its
`Content-Type`, a malicious page could submit a JSON body labelled `text/plain` with the victim's cookies.
Requiring `application/json` forces a preflight, and the browser sends nothing unless the server's CORS policy allows
it. This is why §5.1 keeps the `415` check.

### No client-controlled state
Nothing is cached per header value, so varying `Content-Type` can't grow memory.

---

## 8. Trade-offs

- An endpoint can't offer JSON and CBOR, or accept JSON and form data. Two endpoints can.
- `Accept` mismatches get the serde's format instead of `406`. RFC 9110 permits this, and clients that check
  `Content-Type` still see what they got.
- Multipart (#54) doesn't fit yet: the `boundary` is per request and uploads need streaming.

---

## 9. Migration

1. Add `Serde` and `@CustomSerde` with a default qualifier.
2. Codegen (`EndpointMethodModelAstGen`, `ParamModelAstGen`): inject the endpoint's serde, add the `415` checks, set
   the response `Content-Type`.
3. Move `FastjsonSerde` and `ReqResPlainSerde` to `Serde`; delete `PlainTextSerdeCatalog`.
4. Delete `SerdeCatalog`, `MediaTypeSerdeCatalog`, `@CustomSerdeCatalog` and `serde.deprecated`.
5. Close #30 (the single `KType` `deserialize`) and #46's negotiation items as won't-do.

---

## 10. Open Questions

- Should an endpoint without a body parameter still reject a request that sends one?
- Multipart (#54): a serde bound per request, e.g. `deserializerFor(mediaType)`, or a separate streaming API?
