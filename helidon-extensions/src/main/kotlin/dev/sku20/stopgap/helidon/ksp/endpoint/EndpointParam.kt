package dev.sku20.stopgap.helidon.ksp.endpoint

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

sealed interface EndpointParam {
    data object Request : EndpointParam
    data object Response : EndpointParam
    data class Path(val name: String) : EndpointParam
    data class Query(val name: String) : EndpointParam
    data class Header(val name: String) : EndpointParam
    data class Auth(val type: KSType) : EndpointParam
    data class Body(val type: KSType) : EndpointParam

    companion object {
        fun from(param: KSValueParameter): EndpointParam {
            val type = param.type.resolve()
            val qualifiedName = type.declaration.qualifiedName?.asString()

            if (qualifiedName == ServerRequest::class.qualifiedName) return Request
            if (qualifiedName == ServerResponse::class.qualifiedName) return Response

            val pathParam = param.findAnnotation(PathParam::class)
            if (pathParam != null) return Path(pathParam.argument("name"))

            val queryParam = param.findAnnotation(QueryParam::class)
            if (queryParam != null) return Query(queryParam.argument("name"))

            val headerParam = param.findAnnotation(HeaderParam::class)
            if (headerParam != null) return Header(headerParam.argument("name"))

            if (implementsAuthentication(type)) {
                return Auth(type)
            }

            return Body(type)
        }

        private fun implementsAuthentication(type: KSType): Boolean {
            val authQName = Authentication::class.qualifiedName!!
            if (type.declaration.qualifiedName?.asString() == authQName) return true
            return (type.declaration as? KSClassDeclaration)?.let { isSubtypeOf(it, authQName) } ?: false
        }

        private fun isSubtypeOf(decl: KSClassDeclaration, targetQName: String): Boolean {
            for (superType in decl.superTypes) {
                val superDecl = superType.resolve().declaration
                if (superDecl.qualifiedName?.asString() == targetQName) return true
                if (superDecl is KSClassDeclaration && isSubtypeOf(superDecl, targetQName)) return true
            }
            return false
        }
    }
}
