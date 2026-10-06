package dev.sku20.stopgap.helidon.ksp.route.param

import dev.sku20.stopgap.helidon.ksp.route.TypeModel

sealed interface ParamModel

data object RequestParamModel : ParamModel

data object ResponseParamModel : ParamModel

data class PathParamModel(
    val name: String,
) : ParamModel

data class QueryParamModel(
    val name: String,
) : ParamModel

data class HeaderParamModel(
    val name: String,
) : ParamModel

data object AuthParamModel : ParamModel

data class BodyParamModel(
    val type: TypeModel,
) : ParamModel
