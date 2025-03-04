package com.r0adkll.danger.action

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.r0adkll.danger.Command
import com.r0adkll.danger.DangerBundle
import com.r0adkll.danger.run.DangerRunConfiguration
import com.r0adkll.danger.run.DangerRunConfigurationType
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import org.jetbrains.plugins.github.GithubIcons

class DangerRunAction(
  private val command: Command,
  private val dangerEntrypointElement: PsiElement,
) : AnAction() {

  private val dangerFile
    get() = dangerEntrypointElement.containingFile.originalFile.virtualFile.toNioPath()

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

  override fun update(e: AnActionEvent) {
    super.update(e)

    with(e.presentation) {
      when (command) {
        is Command.Local -> {
          text =
            DangerBundle.message("run.action.local.text", "'${command.base}'", command.numCommits)
          description = DangerBundle.message("run.action.local.description")
          icon = AllIcons.Actions.Diff
        }

        is Command.PR -> {
          text = DangerBundle.message("run.action.pr.text", command.url)
          description = DangerBundle.message("run.action.pr.description")
          icon = GithubIcons.PullRequestsToolWindow
        }
      }
    }
  }

  override fun actionPerformed(event: AnActionEvent) {
    thisLogger().info("Running Dangerfile: $dangerFile")

    // Create and add the run configuration
    val runManager = RunManager.getInstance(event.project!!)
    val configuration =
      runManager.createConfiguration(
        DangerBundle.message(
          "run.action.configuration.name",
          dangerFile.fileName.name,
          command.option.name.uppercase(),
        ),
        DangerRunConfigurationType::class.java,
      )
    runManager.addConfiguration(configuration)

    // Pass the configuration on to it.
    (configuration.configuration as DangerRunConfiguration).applyOptions { options ->
      options.dangerFilePath = dangerFile.absolutePathString()
      options.command = command.option
      if (command is Command.PR) {
        options.prUrl = command.url
      }
      if (command is Command.Local) {
        // TODO: Need to test this more
        // options.stagedOnly = command.useStagedChanges
        options.baseBranch = command.base
      }
    }

    // Execute
    ProgramRunnerUtil.executeConfiguration(
      configuration,
      DefaultRunExecutor.getRunExecutorInstance(),
    )
  }
}
