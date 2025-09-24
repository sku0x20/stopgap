package com.example.stopgap

import com.example.stopgap.instanceregistry.Config
import io.helidon.config.Config as HeliconC


class HelidonConfig(
    private val source: HeliconC
) : Config {

    override fun get(key: String): String {
        return source.get(key).asString().orElseThrow()
    }

    fun getConfig(key: String): HeliconC {
        return source.get(key)
    }

    companion object {
        fun loadDefault(): HelidonConfig {
            return HelidonConfig(HeliconC.create())
        }
    }
}
