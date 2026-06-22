package dev.sku20.helidon.ksp.annotation

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.ksp.argument
import dev.sku20.helidon.ksp.findAnnotation

class EndpointData(val path: String) {
    companion object {
        fun from(clazzDecl: KSClassDeclaration): EndpointData {
            val annotation = clazzDecl.findAnnotation(Endpoint::class)!!
            return EndpointData(annotation.argument("path"))
        }
    }
}
