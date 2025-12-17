plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

//    implementation(libs.plugins.kotlin.jvm)
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:${libs.plugins.kotlin.jvm.get().version}")
}