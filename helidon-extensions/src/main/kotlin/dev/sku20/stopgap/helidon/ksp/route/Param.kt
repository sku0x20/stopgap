package dev.sku20.stopgap.helidon.ksp.route

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

data class AuthParamModel(
    val type: String,
) : ParamModel

data class BodyParamModel(
    val type: TypeModel,
) : ParamModel
