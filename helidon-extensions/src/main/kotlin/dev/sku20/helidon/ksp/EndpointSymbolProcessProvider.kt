package dev.sku20.helidon.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class EndpointSymbolProcessProvider : SymbolProcessorProvider {

    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return EndpointSymbolProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }

}