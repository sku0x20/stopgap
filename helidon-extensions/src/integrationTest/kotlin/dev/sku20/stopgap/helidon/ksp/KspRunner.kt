package dev.sku20.stopgap.helidon.ksp

import com.google.devtools.ksp.impl.KotlinSymbolProcessing
import com.google.devtools.ksp.processing.KSPJvmConfig
import com.google.devtools.ksp.processing.KspGradleLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import dev.sku20.stopgap.helidon.serde.CustomSerdeCatalog
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass

// runs KSP2 in-process over [source] and returns what [block] produced in the first round.
class KspRunner<T>(private val source: String, private val block: (Resolver) -> T) {

    private val results = mutableListOf<T>()

    fun run(): T {
        val dir = Files.createTempDirectory("ksp").toFile()
        val sourceDir = File(dir, "src")
        sourceDir.mkdirs()
        File(sourceDir, "Source.kt").writeText(source)
        val outputDir = File(dir, "out")

        val builder = KSPJvmConfig.Builder()
        builder.moduleName = "main"
        builder.sourceRoots = listOf(sourceDir)
        builder.javaSourceRoots = emptyList()
        builder.commonSourceRoots = emptyList()
        builder.libraries = listOf(location(Unit::class), location(CustomSerdeCatalog::class))
        builder.friends = emptyList()
        builder.processorOptions = emptyMap()
        builder.projectBaseDir = dir
        builder.outputBaseDir = outputDir
        builder.cachesDir = File(outputDir, "caches")
        builder.classOutputDir = File(outputDir, "classes")
        builder.kotlinOutputDir = File(outputDir, "kotlin")
        builder.javaOutputDir = File(outputDir, "java")
        builder.resourceOutputDir = File(outputDir, "resources")
        builder.jdkHome = File(System.getProperty("java.home"))
        builder.jvmTarget = "21"
        builder.languageVersion = "2.3"
        builder.apiVersion = "2.3"

        val exitCode = KotlinSymbolProcessing(
            builder.build(),
            listOf(Provider()),
            KspGradleLogger(KspGradleLogger.LOGGING_LEVEL_WARN)
        ).execute()
        check(exitCode == KotlinSymbolProcessing.ExitCode.OK) { "ksp failed: $exitCode" }
        return results.first()
    }

    private fun location(klass: KClass<*>): File =
        File(klass.java.protectionDomain.codeSource.location.toURI())

    private inner class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = Processor()
    }

    private inner class Processor : SymbolProcessor {
        override fun process(resolver: Resolver): List<KSAnnotated> {
            results.add(block(resolver))
            return emptyList()
        }
    }
}
