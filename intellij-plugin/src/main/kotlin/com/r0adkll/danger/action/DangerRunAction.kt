package com.r0adkll.danger.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.r0adkll.danger.services.dangerKotlin

class DangerRunAction(private val dangerEntrypointElement: PsiElement) : AnAction() {

  override fun actionPerformed(event: AnActionEvent) {
    val dangerFile = dangerEntrypointElement.containingFile.originalFile.virtualFile.path
    thisLogger().warn("Running Dangerfile: ${dangerFile}")

    val danger = event.project?.dangerKotlin() ?: return

    if (danger.exists()) {
      danger.runLocal(dangerFile)
    } else {
      // TODO: We should show a more graceful handling of this, whether an error notification
      //  or prompt UI to instruct user to install danger/danger-kotlin
      error("danger-kotlin is not installed!")
    }

    TODO("Not yet implemented")
  }
}
