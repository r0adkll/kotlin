import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  alias(libs.plugins.mavenPublish)
}

dependencies { implementation(libs.kotlin.stdlib.jdk8) }

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}
