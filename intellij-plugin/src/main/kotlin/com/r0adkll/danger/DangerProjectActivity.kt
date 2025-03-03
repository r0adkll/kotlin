package com.r0adkll.danger

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.r0adkll.danger.services.danger
import com.r0adkll.danger.services.gitHubService
import com.r0adkll.danger.services.gitService
import kotlinx.coroutines.*

/** Called by the IDE whenever a project is loaded */
class DangerProjectActivity : ProjectActivity {

  override suspend fun execute(project: Project) {
    withContext(Dispatchers.EDT) {
      // Load the danger configuration and load the script definition source
      val deferred = mutableListOf<Deferred<*>>()
      deferred += async { project.danger().load() }
      deferred += async {
        project.gitService().load()
        project.gitHubService().findCurrentPullRequestUrl()
      }
      deferred.awaitAll()
    }
  }
}
