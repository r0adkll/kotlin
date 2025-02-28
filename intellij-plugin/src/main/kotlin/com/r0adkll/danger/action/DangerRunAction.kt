package com.r0adkll.danger.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement

class DangerRunAction(
  private val dangerEntrypointElement: PsiElement,
) : AnAction() {

  override fun actionPerformed(event: AnActionEvent) {
    thisLogger().warn("Running Dangerfile: ${dangerEntrypointElement.containingFile.originalFile.virtualFile.path}")

    TODO("Not yet implemented")
  }
}
