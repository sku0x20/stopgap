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

    fun parse(): ParamModel = when {
        isType(ServerRequest::class) -> RequestParamModel
        isType(ServerResponse::class) -> ResponseParamModel
        hasAnnotation(PathParam::class) -> PathParamModel(nameArgument(PathParam::class))
        hasAnnotation(QueryParam::class) -> QueryParamModel(nameArgument(QueryParam::class))
        hasAnnotation(HeaderParam::class) -> HeaderParamModel(nameArgument(HeaderParam::class))
        isAuthentication() -> AuthParamModel(fqn)
        else -> BodyParamModel(toTypeModel(type))
    }

    private fun isType(klass: KClass<*>): Boolean = fqn == klass.qualifiedName

    private fun hasAnnotation(klass: KClass<*>): Boolean = param.findAnnotation(klass) != null

    private fun nameArgument(klass: KClass<*>): String = param.findAnnotation(klass)!!.argument("name")

    private fun isAuthentication(): Boolean {
        val authFqn = Authentication::class.qualifiedName!!
        val declaration = type.declaration
        if (declaration.qualifiedName?.asString() == authFqn) return true
        if (declaration !is KSClassDeclaration) return false
        for (superType in declaration.getAllSuperTypes()) {
            if (superType.declaration.qualifiedName?.asString() == authFqn) return true
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
