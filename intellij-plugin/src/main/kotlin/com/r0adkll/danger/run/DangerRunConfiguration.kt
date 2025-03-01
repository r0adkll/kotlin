package com.r0adkll.danger.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class DangerRunConfigurationType :
  ConfigurationTypeBase(
    id = ID,
    displayName = "Danger Run",
    description = "Run a Dangerfile.df.kts",
    icon = NotNullLazyValue.createValue { AllIcons.Nodes.Console },
  ) {
  init {
    addFactory(DangerKotlinRunConfigurationFactory(this))
  }

  companion object {
    const val ID = "DangerRunConfiguration"
  }
}

class DangerKotlinRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

  override fun getId(): String = DangerRunConfigurationType.ID

  override fun createTemplateConfiguration(project: Project): RunConfiguration {
    return DangerRunConfiguration(project, this, "Run Danger")
  }

  override fun getOptionsClass(): Class<out BaseState> = DangerRunConfigurationOptions::class.java
}

class DangerRunConfigurationOptions : RunConfigurationOptions() {

  private val _dangerFilePath = string(null).provideDelegate(this, "dangerFileName")

  var dangerFilePath: String?
    get() = _dangerFilePath.getValue(this)
    set(value) {
      _dangerFilePath.setValue(this, value)
    }
}

class DangerRunConfiguration(project: Project, factory: ConfigurationFactory?, name: String?) :
  RunConfigurationBase<DangerRunConfigurationOptions>(project, factory, name) {

  var dangerFilePath: String?
    get() = options.dangerFilePath
    set(value) {
      options.dangerFilePath = value
    }

  override fun getOptions(): DangerRunConfigurationOptions {
    return super.getOptions() as DangerRunConfigurationOptions
  }

  override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState? {
    return object : CommandLineState(env) {

      override fun startProcess(): ProcessHandler {
        val commandLine =
          GeneralCommandLine(
              "danger-kotlin",
              "local",
              "--base",
              "main",
              "-d",
              options.dangerFilePath,
            )
            .apply {
              withWorkingDirectory(project.basePath?.toNioPathOrNull())
              withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.SYSTEM)
            }

        val processHandler =
          ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
      }
    }
  }

  override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
    return DangerRunSettingsEditor()
  }
}

class DangerRunSettingsEditor : SettingsEditor<DangerRunConfiguration>() {

  private val panel: JPanel
  private val dangerFilePathField: TextFieldWithBrowseButton = TextFieldWithBrowseButton()

  init {
    dangerFilePathField.addBrowseFolderListener(
      null,
      FileChooserDescriptorFactory.createSingleFileDescriptor(".df.kts"),
    )
    panel =
      FormBuilder.createFormBuilder().addLabeledComponent("Dangerfile", dangerFilePathField).panel
  }

  override fun resetEditorFrom(config: DangerRunConfiguration) {
    dangerFilePathField.setText(config.dangerFilePath)
  }

  override fun applyEditorTo(config: DangerRunConfiguration) {
    config.dangerFilePath = dangerFilePathField.text
  }

  override fun createEditor(): JComponent {
    return panel
  }
}
