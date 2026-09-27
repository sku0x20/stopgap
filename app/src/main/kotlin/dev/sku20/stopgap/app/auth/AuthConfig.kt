package dev.sku20.stopgap.app.auth

import dev.sku20.stopgap.ir.Creates

object AuthConfig {

    @Creates
    fun secureEndpoint(): SecureEndpoint {
        return SecureEndpoint()
    }
}
