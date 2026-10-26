package dev.sku20.stopgap.helidon.mapper.bodyreqmapper.mediatyperule

import io.helidon.http.HttpMediaType

class CompositeMediaTypeRule(rules: List<MediaTypeRule> = emptyList()) : MediaTypeRule {
    private val rules = rules.toMutableList()

    fun add(rule: MediaTypeRule) {
        rules.add(rule)
    }

    override fun accepts(mediaType: HttpMediaType?): Boolean = rules.any { it.accepts(mediaType) }
}
