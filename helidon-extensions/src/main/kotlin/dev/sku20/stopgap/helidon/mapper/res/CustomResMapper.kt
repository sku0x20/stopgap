package dev.sku20.stopgap.helidon.mapper.res

import kotlin.reflect.KClass

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomResMapper(
    val qualifier: String = "",
    val clazz: KClass<out ResMapper> = ResMapper::class
) {
    companion object {
        const val DEFAULT_QUALIFIER = "dev.sku20.stopgap.helidon.mapper.res.default"
    }
}
