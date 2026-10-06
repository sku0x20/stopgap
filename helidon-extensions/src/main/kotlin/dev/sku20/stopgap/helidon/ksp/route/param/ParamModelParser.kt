package dev.sku20.stopgap.helidon.ksp.route.param

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.param.HeaderParam
import dev.sku20.stopgap.helidon.param.PathParam
import dev.sku20.stopgap.helidon.param.QueryParam
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

private val PARAM_ANNOTATION_NAMES = setOf(
    PathParam::class.simpleName,
    QueryParam::class.simpleName,
    HeaderParam::class.simpleName,
)

class ParamModelParser(private val param: KSValueParameter) {

    private val type = param.type.resolve()
    private val fqn = type.declaration.qualifiedName!!.asString()
    private val paramAnnotation = findParamAnnotation()

    fun parse(): ParamModel =
        parseServerParam() ?: parseAnnotatedParam() ?: parseTypedParam()

    private fun parseServerParam(): ParamModel? = when (fqn) {
        ServerRequest::class.qualifiedName -> RequestParamModel
        ServerResponse::class.qualifiedName -> ResponseParamModel
        else -> null
    }

    private fun parseAnnotatedParam(): ParamModel? {
        val annotation = paramAnnotation ?: return null
        val name = annotation.argument<String>("name")
        return when (annotation.shortName.asString()) {
            PathParam::class.simpleName -> PathParamModel(name)
            QueryParam::class.simpleName -> QueryParamModel(name)
            HeaderParam::class.simpleName -> HeaderParamModel(name)
            else -> null
        }
    }

    private fun findParamAnnotation(): KSAnnotation? {
        var found: KSAnnotation? = null
        for (annotation in param.annotations) {
            if (annotation.shortName.asString() !in PARAM_ANNOTATION_NAMES) continue
            if (found != null) {
                throw IllegalArgumentException("Multiple param annotations on: ${param.name!!.asString()}")
            }
            found = annotation
        }
        return found
    }

    private fun parseTypedParam(): ParamModel = when {
        isAuthentication() -> AuthParamModel(fqn)
        else -> BodyParamModel(toTypeModel(type))
    }

    private fun isAuthentication(): Boolean {
        val declaration = type.declaration
        if (declaration !is KSClassDeclaration) return false
        // raw Authentication doesn't make sense.
        if (declaration.qualifiedName?.asString() == Authentication::class.qualifiedName!!) {
            throw IllegalArgumentException("Raw Authentication not supported in param: ${param.name!!.asString()}, use a concrete subtype")
        }
        if (declaration.getAllSuperTypes().any {
                it.declaration.qualifiedName?.asString() == Authentication::class.qualifiedName!!
            }) {
            return true
        }
        return false
    }

    private fun toTypeModel(type: KSType): TypeModel {
        val typeArguments = mutableListOf<TypeModel>()
        for (argument in type.arguments) {
            val argumentType = argument.type
                ?: throw IllegalArgumentException("Star projection not supported in body param: ${param.name!!.asString()}")
            typeArguments.add(toTypeModel(argumentType.resolve()))
        }
        return TypeModel(
            type.declaration.qualifiedName!!.asString(),
            typeArguments,
            type.isMarkedNullable,
        )
    }
}
