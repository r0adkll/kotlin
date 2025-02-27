package com.r0adkll.danger

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.r0adkll.danger.services.danger
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource

class DangerScriptDefinitionsSource(private val project: Project) : ScriptDefinitionsSource {

  private val logger = thisLogger()

  override val definitions: Sequence<ScriptDefinition>
    get() =
      project.danger().dangerScriptDefinition?.let { scriptDefinition ->
        logger.info("--> Loading Danger script source for $scriptDefinition")
        sequenceOf(scriptDefinition)
      } ?: emptySequence()
}
