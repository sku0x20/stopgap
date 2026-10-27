# RFC-0003: Request and Response Mappers

- **Date**: 2026-10-27
- **Target**: `stopgap` / `helidon-extensions`
- **Scope**: Turning a request into an endpoint's body parameter, and an endpoint's return value into a response

---

## 1. Summary

Every endpoint method has exactly one `ReqMapper` and one `ResMapper`. The `ReqMapper` turns the whole request into the
body parameter; the `ResMapper` writes the return value to the response. The toolkit picks neither by media type: an
annotation or the default picks it at compile time, and the mapper decides everything else.

The toolkit ships good defaults built from small, swappable parts (JSON and plain text). Anything they don't cover, such
as content negotiation, vendor media types or multipart, is a mapper the user writes and names on the endpoint.

---

## 2. Design

### Interfaces

```kotlin
interface ReqMapper {
    fun <T> map(request: ServerRequest, type: KType): T
}

interface ResMapper {
    fun <T> map(response: ServerResponse, value: T)
}
```

A mapper gets Helidon's `ServerRequest` or `ServerResponse`, not a narrowed view: headers, content type, the raw body
stream and the status are all in reach.

### Choosing a mapper

`@CustomReqMapper` and `@CustomResMapper` name a mapper on an endpoint class or method, either by registry qualifier or
by class:

```kotlin
@Post("/desPlainText")
@CustomReqMapper(clazz = PlainTextReqMapper::class)
fun desPlainText(res: ServerResponse, body: String) { ... }
```

The method annotation wins over the class annotation, and the class annotation wins over the default, which is the
`ReqMapper`/`ResMapper` bound under `CustomReqMapper.DEFAULT_QUALIFIER`/`CustomResMapper.DEFAULT_QUALIFIER`. The app
binds both:

```kotlin
@Creates
@Qualifier(CustomReqMapper.DEFAULT_QUALIFIER)
fun defaultReqMapper(): ReqMapper {
    return JsonReqMapper(FastJsonDecoder())
}

@Creates
@Qualifier(CustomResMapper.DEFAULT_QUALIFIER)
fun defaultResMapper(): ResMapper {
    return JsonResMapper(FastJsonEncoder())
}
```

A mapper named by class needs its own `@Creates`.

### Generated code

KSP resolves each endpoint's mappers into `registerRoutesFor` parameters, which the registry fills. A route calls the
mapper directly; there is no lookup at request time:

```kotlin
rules.post("/desObj", { req, res ->
    endpoint.desObj(res, reqMapper.map<ReqDto>(req, desObjBodyKType))
})
rules.get("/serObj", { req, res ->
    val resp = endpoint.serObj()
    resMapper.map(res, resp)
})
```

`ReqMapper` is only injected for methods with a body parameter, `ResMapper` only for methods that return a value.

### Shipped building blocks

| Request side                                      | Response side                                     |
|---------------------------------------------------|---------------------------------------------------|
| `BodyReqMapper(MediaTypeRule, BodyDecoder)`       | `BodyResMapper(HttpMediaType, BodyEncoder)`       |
| `JsonReqMapper(decoder)` = `JsonMediaTypeRule`    | `JsonResMapper(encoder)` = `application/json`     |
| `PlainTextReqMapper` = UTF-8 `text/plain` only    | `PlainTextResMapper` = `text/plain; charset=UTF-8` |
| `CompositeMediaTypeRule` (any of several rules)   |                                                   |

`BodyReqMapper` checks the content type against the rule (415 otherwise), reads the raw bytes, rejects an empty body
(400) and hands the bytes to the decoder. A decoder throws `MalformedBodyException` (400) for bytes it can't decode.
`BodyResMapper` sets its media type and sends the encoded bytes.

---

## 3. Why This Approach

### Why full control in the mapper?

Mapping a request is policy, and the policies differ: one API wants strict `application/json`, another a vendor type,
another negotiates on `Accept`, another reads multipart. Any rule the toolkit bakes in is wrong for someone, and working
around a built-in rule is harder than writing a mapper. So the toolkit hands the mapper the whole request or response and
gets out of the way. A mapper is a plain interface with one method; anything that implements it plugs in.

### Why no content negotiation in the toolkit?

Most endpoints speak one media type. Negotiation done properly means `Accept` wildcards, `q` values including `q=0`,
`Vary: Accept` and charset handling; the earlier `SerdeCatalog` did it and diverged from RFC 9110 in several places
(#46). Keeping it out of the toolkit removes that whole class of bugs for the common case. An endpoint that needs
negotiation gets it from a mapper that reads `Accept` and picks an encoder.

### Why one mapper per endpoint, chosen at compile time?

The endpoint's annotations say exactly which mapper runs, so reading the endpoint tells you how its body is parsed.
KSP turns that into a typed parameter of `registerRoutesFor`: a missing mapper fails at startup in the registry, not on
the first request, and a route does no lookup per request.

### Why method, then class, then default?

Most endpoints use the app-wide default and need no annotation. A class annotation switches a whole endpoint (e.g. a
plain-text API), and a method annotation handles the odd one out. The nearest declaration wins.

### Why a qualifier or a class?

Annotation arguments can only hold constants and `KClass`es, not instances. A class is the shortest way to name a
concrete mapper. A qualifier names a role instead (`"xml"`), so the bound implementation can change without touching
endpoints; the default is just a well-known qualifier.

### Why split a body mapper into a rule and a codec?

"Which content types do I accept" and "how do I turn bytes into objects" change independently. Keeping them as separate
strategies lets them combine freely:

- `JsonReqMapper` takes any `BodyDecoder`, so the app picks the JSON library (fastjson2, Jackson, ...) without touching
  the HTTP checks.
- `BodyReqMapper(CompositeMediaTypeRule(listOf(JsonMediaTypeRule(), vendorRule)), decoder)` accepts a vendor type with
  the same decoder.
- The 415/400 handling lives once in `BodyReqMapper` and every decoder gets it.

Fusing the two (one object that both accepts and decodes) was tried and dropped: one rule could no longer pair with
several decoders, and a composite of them could not tell which child accepted.

### Why does the toolkit ship no JSON library?

`JsonReqMapper`/`JsonResMapper` only fix the media type; the codec comes from the app. The library choice, its
configuration (e.g. a Kotlin `Uuid` reader) and its upgrades stay with the app, and `helidon-extensions` does not drag
in a JSON dependency.

### Why strict defaults?

A default should never accept a body it wasn't meant for. `JsonMediaTypeRule` accepts `application/json` and
`application/*+json` and compares type and subtype directly, because Helidon's `HttpMediaType.test()` treats `*/*` as a
match. `PlainTextMediaTypeRule` accepts only UTF-8 (or no charset), so the decoder never guesses an encoding. Anything
looser is a rule the user adds.

### Why raw bytes, not Helidon's entity readers?

The mapper owns decoding end to end. `BodyReqMapper` reads `content().inputStream()` and never `content().as(...)`, so no
Helidon media support runs in between. It also behaves on an empty body: `as()` throws `IllegalStateException` (a 500),
while the stream is simply empty and becomes a 400.

### Why does the response mapper ignore `Accept`?

The endpoint's `ResMapper` decides the representation, the same way its `ReqMapper` decides what it accepts. The
response always carries the mapper's media type, so there is nothing to vary and no 406 to send. JSON goes out as plain
`application/json`: RFC 8259 defines no `charset` parameter for it.

### Why named exceptions?

Each failure cause has its own `HttpException` subclass with its status (`UnsupportedMediaTypeException` 415,
`EmptyBodyException` and `MalformedBodyException` 400). Custom mappers can throw the same ones, so clients see the same
errors whichever mapper ran.

---

## 4. Trade-offs

None of substance. A need the defaults don't cover, including content negotiation, is a custom mapper.
