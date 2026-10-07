package dev.sku20.stopgap.helidon.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

// TODO: annotations are matched by simple name across ksp (grep `shortName`), so a same-named
//  annotation from another library also matches. Match by qualified name instead.
fun KSAnnotated.findAnnotation(klass: KClass<*>): KSAnnotation? =
    annotations.firstOrNull { it.shortName.asString() == klass.simpleName }

@Suppress("UNCHECKED_CAST")
fun <T> KSAnnotation.argument(name: String): T =
    arguments.first { it.name?.getShortName() == name }.value as T
