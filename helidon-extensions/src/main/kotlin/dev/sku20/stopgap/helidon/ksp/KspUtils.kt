package dev.sku20.stopgap.helidon.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

fun KSAnnotated.findAnnotation(klass: KClass<*>): KSAnnotation? =
    annotations.firstOrNull { it.qualifiedName() == klass.qualifiedName }

fun KSAnnotation.qualifiedName(): String? =
    annotationType.resolve().declaration.qualifiedName?.asString()

@Suppress("UNCHECKED_CAST")
fun <T> KSAnnotation.argument(name: String): T =
    arguments.first { it.name?.getShortName() == name }.value as T
