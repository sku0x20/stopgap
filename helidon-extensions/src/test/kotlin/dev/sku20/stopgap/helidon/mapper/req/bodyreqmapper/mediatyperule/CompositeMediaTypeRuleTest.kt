package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.mediatyperule

import io.helidon.http.HttpMediaTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CompositeMediaTypeRuleTest {

    private val accepting = mock<MediaTypeRule>()
    private val rejecting = mock<MediaTypeRule>()
    private val mediaType = HttpMediaTypes.JSON

    @BeforeEach
    fun setup() {
        whenever(accepting.accepts(mediaType)).thenReturn(true)
        whenever(rejecting.accepts(mediaType)).thenReturn(false)
    }

    @Test
    fun rejectsWhenEmpty() {
        assertThat(CompositeMediaTypeRule().accepts(mediaType)).isFalse()
    }

    @Test
    fun acceptsWhenAnyRuleAccepts() {
        val rule = CompositeMediaTypeRule(listOf(rejecting, accepting))

        assertThat(rule.accepts(mediaType)).isTrue()
    }

    @Test
    fun rejectsWhenAllRulesReject() {
        val rule = CompositeMediaTypeRule(listOf(rejecting, rejecting))

        assertThat(rule.accepts(mediaType)).isFalse()
    }

    @Test
    fun acceptsWithAddedRule() {
        val rule = CompositeMediaTypeRule(listOf(rejecting))
        rule.add(accepting)

        assertThat(rule.accepts(mediaType)).isTrue()
    }

    @Test
    fun stopsAtFirstAcceptingRule() {
        val rule = CompositeMediaTypeRule(listOf(accepting, rejecting))

        rule.accepts(mediaType)

        verify(rejecting, never()).accepts(mediaType)
    }

    @Test
    fun ignoresLaterChangesToConstructorList() {
        val rules = mutableListOf(rejecting)
        val rule = CompositeMediaTypeRule(rules)
        rules.add(accepting)

        assertThat(rule.accepts(mediaType)).isFalse()
    }
}
