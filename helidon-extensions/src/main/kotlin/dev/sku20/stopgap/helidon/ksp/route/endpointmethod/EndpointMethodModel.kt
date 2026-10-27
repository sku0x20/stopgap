package dev.sku20.stopgap.helidon.ksp.route.endpointmethod

import dev.sku20.stopgap.helidon.ksp.route.customreqmapper.CustomReqMapperModel
import dev.sku20.stopgap.helidon.ksp.route.customresmapper.CustomResMapperModel
import dev.sku20.stopgap.helidon.ksp.route.customserdecatalog.CustomSerdeCatalogModel
import dev.sku20.stopgap.helidon.ksp.route.param.AuthParamModel
import dev.sku20.stopgap.helidon.ksp.route.param.ParamModel

data class EndpointMethodModel(
    val name: String,
    val httpMethod: HttpMethod,
    val path: String,
    val customSerdeCatalogModel: CustomSerdeCatalogModel? = null,
    val customReqMapperModel: CustomReqMapperModel? = null,
    val customResMapperModel: CustomResMapperModel? = null,
    // its a param but to save lookup since it has to be on each method.
    val authParamModel: AuthParamModel? = null,
    val params: List<ParamModel> = emptyList(),
    val isResponseUnit: Boolean = true,
)
