package com.example.stopgap.param

import dev.sku20.ir.Creates

object ParamConfig {

    @Creates
    fun paramEndpoint(): ParamEndpoint {
        return ParamEndpoint()
    }

}
