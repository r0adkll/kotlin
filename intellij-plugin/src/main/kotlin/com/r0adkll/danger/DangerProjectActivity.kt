package com.r0adkll.danger

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.r0adkll.danger.services.danger

/**
 * Called by the IDE whenever a project is loaded
 */
class DangerProjectActivity : ProjectActivity {

  override suspend fun execute(project: Project) {
    // Load the danger configuration and load the script definition source
    project.danger().load()
  }
}
