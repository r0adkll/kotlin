package com.r0adkll.danger.action

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.r0adkll.danger.run.DangerRunConfiguration
import com.r0adkll.danger.run.DangerRunConfigurationType
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class DangerRunAction(private val dangerEntrypointElement: PsiElement) : AnAction() {

  override fun actionPerformed(event: AnActionEvent) {
    val dangerFile = dangerEntrypointElement.containingFile.originalFile.virtualFile.toNioPath()
    thisLogger().info("Running Dangerfile: ${dangerFile}")

    // Create and add the run configuration
    val runManager = RunManager.getInstance(event.project!!)
    val configuration = runManager.createConfiguration(
      "Run ${dangerFile.fileName.name}",
      DangerRunConfigurationType::class.java,
    )
    runManager.addConfiguration(configuration)

    // Hydrate with the passed dangerfile
    val dangerRunConfiguration = (configuration.configuration as DangerRunConfiguration)
    dangerRunConfiguration.dangerFilePath = dangerFile.absolutePathString()

    // Execute
    ProgramRunnerUtil.executeConfiguration(configuration, DefaultRunExecutor.getRunExecutorInstance())
  }
}
