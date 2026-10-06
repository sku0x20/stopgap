package dev.sku20.stopgap.helidon.ksp.route.param

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.stopgap.helidon.authentication.Authentication
import dev.sku20.stopgap.helidon.ksp.argument
import dev.sku20.stopgap.helidon.ksp.findAnnotation
import dev.sku20.stopgap.helidon.param.HeaderParam
import dev.sku20.stopgap.helidon.param.PathParam
import dev.sku20.stopgap.helidon.param.QueryParam
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import kotlin.reflect.KClass

class ParamModelParser(private val param: KSValueParameter) {

    private val type = param.type.resolve()
    private val fqn = type.declaration.qualifiedName!!.asString()

    fun parse(): ParamModel =
        parseServerParam() ?: parseAnnotatedParam() ?: parseTypedParam()

    private fun parseServerParam(): ParamModel? = when (fqn) {
        ServerRequest::class.qualifiedName -> RequestParamModel
        ServerResponse::class.qualifiedName -> ResponseParamModel
        else -> null
    }

    private fun parseAnnotatedParam(): ParamModel? = when {
        hasAnnotation(PathParam::class) -> PathParamModel(nameArgument(PathParam::class))
        hasAnnotation(QueryParam::class) -> QueryParamModel(nameArgument(QueryParam::class))
        hasAnnotation(HeaderParam::class) -> HeaderParamModel(nameArgument(HeaderParam::class))
        else -> null
    }

    private fun parseTypedParam(): ParamModel = when {
        isAuthentication() -> AuthParamModel(fqn)
        else -> BodyParamModel(toTypeModel(type))
    }

    private fun hasAnnotation(klass: KClass<*>): Boolean =
        param.findAnnotation(klass) != null

    private fun nameArgument(klass: KClass<*>): String =
        param.findAnnotation(klass)!!.argument("name")

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
