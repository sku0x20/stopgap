package dev.sku20.helidon.param

import io.helidon.http.HeaderName
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class HeaderParam(
    val name: KClass<out HeaderName>
)
