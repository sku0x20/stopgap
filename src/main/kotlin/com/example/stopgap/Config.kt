package com.example.stopgap

/**
 * it follows property syntax for now.
 * will be later extended to be more generic.
 */
interface Config {
    fun get(key: String): String
}