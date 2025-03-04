package com.r0adkll.danger.action

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import com.r0adkll.danger.DangerBundle
import com.r0adkll.danger.run.DangerRunConfiguration
import com.r0adkll.danger.run.DangerRunConfigurationType
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class CreateDangerRunAction(private val dangerEntrypointElement: PsiElement) : AnAction() {

  private val dangerFile
    get() = dangerEntrypointElement.containingFile.originalFile.virtualFile.toNioPath()

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    super.update(e)

    with(e.presentation) {
      text = DangerBundle.message("run.action.create.text")
      description = DangerBundle.message("run.action.create.description")
    }
  }

  override fun actionPerformed(event: AnActionEvent) {
    // Create and add the run configuration
    val runManager = RunManager.getInstance(event.project!!)
    val configuration =
      runManager.createConfiguration(
        DangerBundle.message("run.action.configuration.name", dangerFile.fileName.name),
        DangerRunConfigurationType::class.java,
      )
    configuration.isEditBeforeRun = true

    // Pass the configuration on to it.
    (configuration.configuration as DangerRunConfiguration).applyOptions { options ->
      options.dangerFilePath = dangerFile.absolutePathString()
    }

    runManager.addConfiguration(configuration)

    // Execute
    ProgramRunnerUtil.executeConfiguration(
      configuration,
      DefaultRunExecutor.getRunExecutorInstance(),
    )
  }
}
