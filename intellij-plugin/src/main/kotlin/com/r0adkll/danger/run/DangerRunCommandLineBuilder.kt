package com.r0adkll.danger.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType.SYSTEM
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.toNioPathOrNull
import com.r0adkll.danger.Command
import com.r0adkll.danger.CommandOption
import com.r0adkll.danger.services.CiProviderFactory
import kotlinx.coroutines.runBlocking

fun Project.dangerCommandLineBuilder() = service<DangerRunCommandLineBuilder>()

@Service(PROJECT)
class DangerRunCommandLineBuilder(private val project: Project) {

  fun build(
    command: CommandOption,
    prUrl: String?,
    baseBranch: String?,
    stagedOnly: Boolean,
    dangerFilePath: String,
  ): GeneralCommandLine {
    return when (command) {
      CommandOption.LOCAL ->
        buildLocalCommand(
          Command.Local(base = baseBranch ?: "main", useStagedChanges = stagedOnly),
          dangerFilePath,
        )
      CommandOption.PR -> buildPRCommand(Command.PR(requireNotNull(prUrl)), dangerFilePath)
    }.apply {
      withWorkingDirectory(project.basePath?.toNioPathOrNull())
      withParentEnvironmentType(SYSTEM)

      // This should attempt to hydrate this run configuration with the GH token
      // automatically, if the Github integration has an account.
      val ciEnv = runBlocking {
        CiProviderFactory.getInstance(project).provideAvailableService().runEnvironment()
      }
      withEnvironment(ciEnv)
    }
  }

  private fun buildLocalCommand(
    command: Command.Local,
    dangerFilePath: String,
  ): GeneralCommandLine {
    return GeneralCommandLine(
      buildList {
        add("danger-kotlin")
        add("local")

        // If we have staged changes, then send the flag to
        // compare those
        if (command.useStagedChanges) {
          add("--staging")
        }

        // Set the base branch to compare against
        add("--base")
        add(command.base)

        // Specify the dangerfile to evaluate
        add("-d")
        add(dangerFilePath)
      }
    )
  }

  private fun buildPRCommand(command: Command.PR, dangerFilePath: String): GeneralCommandLine {
    return GeneralCommandLine("danger-kotlin", "pr", "-d", dangerFilePath, command.url)
  }
}
