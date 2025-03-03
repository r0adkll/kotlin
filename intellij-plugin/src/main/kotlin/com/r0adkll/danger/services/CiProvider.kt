package com.r0adkll.danger.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

interface CiProvider {

  /** Returns if this provider is available for use */
  suspend fun isAvailable(): Boolean

  /**
   * Return the current pull request URL for the given local branch if any. This is used to provide
   * the `danger pr` run configuration to the user
   */
  suspend fun findCurrentPullRequestUrl(): String?

  /**
   * Return environment variables to use when running danger commands via
   * [com.r0adkll.danger.run.DangerRunConfiguration].
   */
  suspend fun runEnvironment(): Map<String, String>
}

@Service(PROJECT)
class CiProviderFactory(private val project: Project) {

  suspend fun provideAvailableService(): CiProvider {
    // TODO: Support other CI providers here. We should also provide a settings
    //  configuration that let's users select / add custom CI providers like
    //  GitLab, Bitbucket, etc.
    return project.gitHubService()
  }

  companion object {
    fun getInstance(project: Project) = project.service<CiProviderFactory>()
  }
}
