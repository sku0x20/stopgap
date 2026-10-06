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

class ParamModelParser(private val param: KSValueParameter) {

    fun parse(): ParamModel {
        val type = param.type.resolve()
        val fqn = type.declaration.qualifiedName!!.asString()

        if (fqn == ServerRequest::class.qualifiedName) return RequestParamModel
        if (fqn == ServerResponse::class.qualifiedName) return ResponseParamModel

        val pathParam = param.findAnnotation(PathParam::class)
        if (pathParam != null) return PathParamModel(pathParam.argument("name"))

        val queryParam = param.findAnnotation(QueryParam::class)
        if (queryParam != null) return QueryParamModel(queryParam.argument("name"))

        val headerParam = param.findAnnotation(HeaderParam::class)
        if (headerParam != null) return HeaderParamModel(headerParam.argument("name"))

        if (isAuthentication(type)) return AuthParamModel(fqn)

        return BodyParamModel(toTypeModel(type))
    }

    private fun isAuthentication(type: KSType): Boolean {
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
            typeArguments.add(toTypeModel(argument.type!!.resolve()))
        }
        return TypeModel(
            type.declaration.qualifiedName!!.asString(),
            typeArguments,
            type.isMarkedNullable,
        )
    }
}
