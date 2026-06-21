package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * Resolves a [Serde] for a given media type.
 *
 * Implementations are free to choose how that resolution happens, e.g. a lookup
 * keyed by media type, a switch over known types, or always returning the same
 * [Serde] regardless of the requested media type. Implement this interface directly
 * for any custom selection strategy.
 *
 * @see MapSerdeCatalog
 */
interface SerdeCatalog {

    /**
     * Resolves a [Serde] for a single media type, e.g. a request's Content-Type.
     *
     * @param mediaType the requested media type, or `null` if the request didn't specify one
     * (e.g. a missing Content-Type header)
     */
    fun get(mediaType: HttpMediaType?): Serde

    /**
     * Resolves a [Serde] for a list of acceptable media types, e.g. a request's Accept header,
     * in preference order — the first entry the implementation can satisfy wins.
     *
     * @param mediaTypes the acceptable media types in preference order, possibly empty
     */
    fun get(mediaTypes: List<HttpMediaType>): Serde
}
