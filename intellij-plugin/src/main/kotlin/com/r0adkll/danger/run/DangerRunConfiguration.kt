package com.r0adkll.danger.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.TextFieldWithStoredHistory
import com.intellij.util.ui.FormBuilder
import com.r0adkll.danger.CommandOption
import com.r0adkll.danger.DangerBundle
import com.r0adkll.danger.DangerIcons
import javax.swing.JComponent
import javax.swing.JPanel

class DangerRunConfigurationType :
  ConfigurationTypeBase(
    id = ID,
    displayName = DangerBundle.message("run.configuration.displayName"),
    description = DangerBundle.message("run.configuration.description"),
    icon = NotNullLazyValue.createValue { DangerIcons.DangerKotlin },
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
    return DangerRunConfiguration(
      project,
      this,
      DangerBundle.message("run.configuration.displayName"),
    )
  }

  override fun getOptionsClass(): Class<out BaseState> = DangerRunConfigurationOptions::class.java
}

class DangerRunConfigurationOptions : RunConfigurationOptions() {

  var dangerFilePath: String? by string(null).provideDelegate(this, ::dangerFilePath)
  var command: CommandOption by
    enum<CommandOption>(CommandOption.LOCAL).provideDelegate(this, ::command)
  var prUrl: String? by string(null).provideDelegate(this, ::prUrl)
  var baseBranch: String? by string(null).provideDelegate(this, ::baseBranch)
  var stagedOnly: Boolean by property(false).provideDelegate(this, ::stagedOnly)
}

class DangerRunConfiguration(project: Project, factory: ConfigurationFactory?, name: String?) :
  RunConfigurationBase<DangerRunConfigurationOptions>(project, factory, name) {

  // Proxy this since this class doesn't let us expose it directly
  val dangerOptions: DangerRunConfigurationOptions
    get() = options

  fun applyOptions(block: (DangerRunConfigurationOptions) -> Unit): DangerRunConfiguration {
    block(options)
    return this
  }

  override fun getOptions(): DangerRunConfigurationOptions {
    return super.getOptions() as DangerRunConfigurationOptions
  }

  override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState {
    return object : CommandLineState(env) {

      override fun startProcess(): ProcessHandler {
        val commandLine =
          project
            .dangerCommandLineBuilder()
            .build(
              command = options.command,
              prUrl = options.prUrl,
              baseBranch = options.baseBranch,
              stagedOnly = options.stagedOnly,
              dangerFilePath = requireNotNull(options.dangerFilePath),
            )

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

// TODO: Add additional options to this editor
class DangerRunSettingsEditor : SettingsEditor<DangerRunConfiguration>() {

  private val panel: JPanel
  private val dangerFilePathField: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
  private val commandDropdownField: ComboBox<CommandOption> =
    ComboBox(EnumComboBoxModel(CommandOption::class.java))
  private val prUrlField: TextFieldWithStoredHistory = TextFieldWithStoredHistory("pullRequestUrl")

  init {
    /*
     * TODO: Remove this when version 243+ is the min supported
     * ```
     * dangerFilePathField.addBrowseFolderListener(
     *   null,
     *   FileChooserDescriptorFactory.createSingleFileDescriptor(".df.kts"),
     * )
     * ```
     */
    @Suppress("DEPRECATION", "removal")
    dangerFilePathField.addBrowseFolderListener(
      null,
      null,
      null,
      FileChooserDescriptorFactory.createSingleFileDescriptor(".df.kts"),
    )

    // TODO: Figure out how to dynamic show/enable these fields based on the command option
    panel =
      FormBuilder.createFormBuilder()
        .addLabeledComponent(
          DangerBundle.message("run.configuration.settings.dangerfile"),
          dangerFilePathField,
        )
        .addLabeledComponent(
          DangerBundle.message("run.configuration.settings.command"),
          commandDropdownField,
        )
        .addLabeledComponent(
          DangerBundle.message("run.configuration.settings.pullRequestUrl"),
          prUrlField,
        )
        .panel
  }

  override fun resetEditorFrom(config: DangerRunConfiguration) {
    dangerFilePathField.setText(config.dangerOptions.dangerFilePath)
    commandDropdownField.item = config.dangerOptions.command
    prUrlField.text = config.dangerOptions.prUrl
  }

  override fun applyEditorTo(config: DangerRunConfiguration) {
    config.dangerOptions.dangerFilePath = dangerFilePathField.text
    config.dangerOptions.command = commandDropdownField.item
    config.dangerOptions.prUrl = prUrlField.text
  }

  override fun createEditor(): JComponent {
    return panel
  }
}
