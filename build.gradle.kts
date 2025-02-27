import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.spotless.LineEnding
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import java.net.URI
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.kotlinSerialization) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.shadowJar) apply false
  alias(libs.plugins.spotless)
}

allprojects {
  apply(plugin = "com.diffplug.spotless")
  val spotlessFormatters: SpotlessExtension.() -> Unit = {
    lineEndings = LineEnding.PLATFORM_NATIVE

    format("misc") {
      target("*.md", ".gitignore")
      endWithNewline()
    }
    kotlin {
      target("src/**/*.kt")
      ktfmt(libs.versions.ktfmt.get()).googleStyle()
      trimTrailingWhitespace()
      endWithNewline()
      targetExclude("**/spotless.kt")
    }
    kotlinGradle {
      target("*.kts")
      ktfmt(libs.versions.ktfmt.get()).googleStyle()
      trimTrailingWhitespace()
      endWithNewline()
    }
  }
  configure<SpotlessExtension> {
    spotlessFormatters()
    if (project.rootProject == project) {
      predeclareDeps()
    }
  }
  if (project.rootProject == project) {
    configure<SpotlessExtensionPredeclare> { spotlessFormatters() }
  }
}

subprojects {
  val isPublished = project.hasProperty("POM_ARTIFACT_ID")

  pluginManager.withPlugin("com.vanniktech.maven.publish") {
    apply(plugin = "org.jetbrains.dokka")

    tasks.withType<DokkaTaskPartial>().configureEach {
      moduleName.set(project.path.removePrefix(":").replace(":", "/"))
      outputDirectory.set(layout.buildDirectory.dir("docs/partial"))
      dokkaSourceSets.configureEach {
        val readMeProvider = project.layout.projectDirectory.file("README.md")
        if (readMeProvider.asFile.exists()) {
          includes.from(readMeProvider)
        }

        if (name.contains("androidTest", ignoreCase = true)) {
          suppress.set(true)
        }
        skipDeprecated.set(true)

        // Skip internal packages
        perPackageOption {
          // language=RegExp
          matchingRegex.set(".*\\.internal\\..*")
          suppress.set(true)
        }
        // AndroidX and Android docs are automatically added by the Dokka plugin.

        // Add source links
        sourceLink {
          localDirectory.set(layout.projectDirectory.dir("src").asFile)
          val relPath = rootProject.projectDir.toPath().relativize(projectDir.toPath())
          remoteUrl.set(
            providers.gradleProperty("POM_SCM_URL").map { scmUrl ->
              URI("$scmUrl/tree/main/$relPath/src").toURL()
            }
          )
          remoteLineSuffix.set("#L")
        }
      }
    }

    configure<MavenPublishBaseExtension> {
      publishToMavenCentral(automaticRelease = true)
      signAllPublications()
    }
  }
}
