import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  alias(libs.plugins.shadowJar)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mavenPublish)
}

dependencies {
  api(project(":danger-kotlin-sdk"))
  api(project(":danger-kotlin-kts"))

  api(libs.kotlinx.coroutines.core)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.serialization.json)
  api(libs.github)
  implementation(libs.kotlin.main.kts)
  implementation(libs.kotlin.stdlib.jdk8)

  implementation(libs.bundles.testing)
}

tasks.named<ShadowJar>("shadowJar") {
  archiveBaseName = "danger-kotlin"
  archiveAppendix = ""
  archiveClassifier = ""
  archiveVersion = ""
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}
