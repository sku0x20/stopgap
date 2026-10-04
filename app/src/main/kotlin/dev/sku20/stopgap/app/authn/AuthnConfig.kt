package dev.sku20.stopgap.app.authn

import dev.sku20.stopgap.ir.annotation.Creates

object AuthnConfig {

    @Creates
    fun secureEndpoint(): SecureEndpoint {
        return SecureEndpoint()
    }
}
