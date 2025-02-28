plugins {
  kotlin("jvm")
  id("java-gradle-plugin")
  alias(libs.plugins.mavenPublish)
}

gradlePlugin {
  plugins {
    create("simplePlugin") {
      id = "danger-kotlin-plugin-installer"
      implementationClass = "systems.danger.kotlin.plugininstaller.PluginInstaller"
    }
  }
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  implementation(libs.kotlin.stdlib.jdk8)
  implementation(gradleApi())
}

tasks {
  test {
    useJUnitPlatform()
    maxHeapSize = "2g"
  }
}
