package com.r0adkll.danger.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.util.io.awaitExit
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch

fun Project.dangerKotlin(): DangerKotlin = service()

@Service(PROJECT)
class DangerKotlin(private val project: Project, private val scope: CoroutineScope) {

  /**
   * Check if the `danger-kotlin` CLI tool is installed
   *
   * TODO: If its NOT, then we should figure out a way to install it, but for now stretch goals
   */
  fun exists(): Boolean {
    val versionCl = GeneralCommandLine("danger-kotlin", "--version")
    val output = ExecUtil.execAndReadLine(versionCl)
    return output?.startsWith(VERSION_PREFIX) == true
  }

  // TODO: We should try to automatically detect what branch the user is on
  //  and if a main/master branch is available. Failing that, we should provide a settings panel
  //  where they can configure their base branch, i.e. 'develop'
  fun runLocal(dangerFile: String) =
    scope.launch(Dispatchers.Default) {
      // Find the danger-js runtime
      val dangerJsPath = ExecUtil.execAndReadLine(GeneralCommandLine("which", "danger"))
        ?: error("Unable to find danger path")

      Runtime.getRuntime()
        .exec(
          arrayOf(
            "danger-kotlin",
            "local",
            "--danger-js-path",
            dangerJsPath,
            "--base",
            "main",
            "-d",
            dangerFile,
          ),
          emptyArray(),
          project.basePath?.let(::File),
        )
        .apply {
          awaitExit()

          inputReader().lineSequence().forEach { this@DangerKotlin.thisLogger().warn("<-- $it") }
          errorReader().lineSequence().forEach { this@DangerKotlin.thisLogger().warn("<!-- $it") }

          toHandle().apply {
            val exitHandle = this.onExit().await()
            this@DangerKotlin.thisLogger().warn("EXIT: ${exitHandle.info()}")

            this.children().forEach { child ->
              this@DangerKotlin.thisLogger()
                .warn("--CHILD: ${child.info().commandLine()}")
            }
          }
        }
    }
}

private const val VERSION_PREFIX = "danger-kotlin version"
