package dev.sku20.stopgap.app.authn

import dev.sku20.stopgap.helidon.authentication.Authentication

data class UserAuthN(
    val id: String
) : Authentication
