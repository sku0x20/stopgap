package dev.sku20.stopgap.helidon.mapper.req

import kotlin.reflect.KClass

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomReqMapper(
    val qualifier: String = "",
    val clazz: KClass<out ReqMapper> = ReqMapper::class
) {
    companion object {
        const val DEFAULT_QUALIFIER = "dev.sku20.stopgap.helidon.mapper.req.default"
    }
}
