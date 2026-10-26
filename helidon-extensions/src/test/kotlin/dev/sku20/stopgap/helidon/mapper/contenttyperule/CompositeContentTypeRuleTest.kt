package dev.sku20.stopgap.helidon.mapper.contenttyperule

import io.helidon.http.HttpMediaTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CompositeContentTypeRuleTest {

    private val accepting = mock<ContentTypeRule>()
    private val rejecting = mock<ContentTypeRule>()
    private val contentType = HttpMediaTypes.JSON

    @BeforeEach
    fun setup() {
        whenever(accepting.accepts(contentType)).thenReturn(true)
        whenever(rejecting.accepts(contentType)).thenReturn(false)
    }

    @Test
    fun rejectsWhenEmpty() {
        assertThat(CompositeContentTypeRule().accepts(contentType)).isFalse()
    }

    @Test
    fun acceptsWhenAnyRuleAccepts() {
        val rule = CompositeContentTypeRule(listOf(rejecting, accepting))

        assertThat(rule.accepts(contentType)).isTrue()
    }

    @Test
    fun rejectsWhenAllRulesReject() {
        val rule = CompositeContentTypeRule(listOf(rejecting, rejecting))

        assertThat(rule.accepts(contentType)).isFalse()
    }

    @Test
    fun acceptsWithAddedRule() {
        val rule = CompositeContentTypeRule(listOf(rejecting))
        rule.add(accepting)

        assertThat(rule.accepts(contentType)).isTrue()
    }

    @Test
    fun stopsAtFirstAcceptingRule() {
        val rule = CompositeContentTypeRule(listOf(accepting, rejecting))

        rule.accepts(contentType)

        verify(rejecting, never()).accepts(contentType)
    }

    @Test
    fun ignoresLaterChangesToConstructorList() {
        val rules = mutableListOf(rejecting)
        val rule = CompositeContentTypeRule(rules)
        rules.add(accepting)

        assertThat(rule.accepts(contentType)).isFalse()
    }
}
