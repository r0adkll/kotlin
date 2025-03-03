package com.r0adkll.danger.gutter

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.r0adkll.danger.Command
import com.r0adkll.danger.action.CreateDangerRunAction
import com.r0adkll.danger.action.DangerRunAction
import com.r0adkll.danger.services.CiProviderFactory
import com.r0adkll.danger.services.gitService
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.psi.KtScript
import org.jetbrains.kotlin.psi.KtScriptInitializer

class DangerRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    return null
  }

  override fun getSlowInfo(element: PsiElement): Info? {
    if (element is LeafPsiElement && element.text == "danger" && element.hasScriptParent()) {
      val gitState = element.project.gitService().gitState
      val pullRequestUrl = runBlocking {
        val ciService = CiProviderFactory.getInstance(element.project).provideAvailableService()
        ciService.findCurrentPullRequestUrl()
      }

      return Info(
        AllIcons.RunConfigurations.TestState.Run,
        buildList<AnAction> {
            // Add the `danger local` action
            gitState?.let { git ->
              if (!git.isBaseBranch && git.hasBaseBranch && git.hasDiffWithBase) {
                add(
                  DangerRunAction(
                    Command.Local(
                      base = git.baseBranch?.name ?: "main",
                      useStagedChanges = git.hasStagedChanges,
                      numCommits = git.diffs.size,
                    ),
                    element,
                  )
                )
              }
            }

            pullRequestUrl?.let { pr -> add(DangerRunAction(Command.PR(pr), element)) }

            add(CreateDangerRunAction(element))
          }
          .toTypedArray(),
      )
    }
    return null
  }

  private fun PsiElement.hasScriptParent(): Boolean {
    return PsiTreeUtil.getParentOfType(this, KtScriptInitializer::class.java) != null ||
      PsiTreeUtil.getParentOfType(this, KtScript::class.java) != null
  }
}
