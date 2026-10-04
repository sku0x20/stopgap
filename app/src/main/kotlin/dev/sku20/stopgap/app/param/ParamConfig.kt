package dev.sku20.stopgap.app.param

import dev.sku20.stopgap.ir.annotation.Creates

object ParamConfig {

    @Creates
    fun paramEndpoint(): ParamEndpoint {
        return ParamEndpoint()
    }

}
