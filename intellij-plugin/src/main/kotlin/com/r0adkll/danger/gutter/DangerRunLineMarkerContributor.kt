package com.r0adkll.danger.gutter

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.r0adkll.danger.action.DangerRunAction
import org.jetbrains.kotlin.psi.KtScript
import org.jetbrains.kotlin.psi.KtScriptInitializer

class DangerRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (
      element is LeafPsiElement &&
      element.text == "danger" &&
      element.hasScriptParent()
    ) {
      return Info(
        AllIcons.RunConfigurations.TestState.Run,
        arrayOf<AnAction>(
          DangerRunAction(element),
        ),
      )
    }
    return null
  }

  private fun PsiElement.hasScriptParent(): Boolean {
    return PsiTreeUtil.getParentOfType(this, KtScriptInitializer::class.java) != null ||
      PsiTreeUtil.getParentOfType(this, KtScript::class.java) != null
  }
}
