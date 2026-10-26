package dev.sku20.stopgap.helidon.mapper.contenttyperule

import io.helidon.http.HttpMediaType

class CompositeContentTypeRule(rules: List<ContentTypeRule> = emptyList()) : ContentTypeRule {
    private val rules = rules.toMutableList()

    fun add(rule: ContentTypeRule) {
        rules.add(rule)
    }

    override fun accepts(contentType: HttpMediaType?): Boolean = rules.any { it.accepts(contentType) }
}
