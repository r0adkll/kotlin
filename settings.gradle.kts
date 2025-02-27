pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
  }
}

plugins {
  // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenCentral()
    mavenLocal()
  }
}

include(
  ":danger-kotlin",
  ":danger-kotlin-library",
  ":danger-kotlin-sdk",
  ":danger-kotlin-kts",
  ":danger-plugin-installer",
)

include(":intellij-plugin")

rootProject.name = "danger-kotlin"
