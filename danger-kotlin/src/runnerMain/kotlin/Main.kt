import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import systems.danger.DangerKotlin
import systems.danger.Log
import systems.danger.cmd.Command
import systems.danger.cmd.dangerjs.DangerJS

const val PROCESS_DANGER_KOTLIN = "danger-kotlin"
const val VERSION = "2.0.0"

data class DangerCommandConfig(var verbose: Boolean = false, var dangerKotlinJar: String? = null)

class DangerCommand(private val originalArgv: Array<String>) :
  CliktCommand(name = "danger-kotlin") {

  init {
    eagerOption("--version") { throw PrintMessage("$PROCESS_DANGER_KOTLIN version $VERSION") }
  }

  private val verbose by option("-v", "--verbose").flag(default = true)

  private val config by findOrSetObject { DangerCommandConfig() }

  override val invokeWithoutSubcommand: Boolean = true

  override fun run() {
    if (verbose) {
      echo("Starting Danger-Kotlin $VERSION with args '${originalArgv.joinToString(", ")}'")
    }

    config.verbose = verbose
    Log.isVerbose = verbose

    // If the CLI was called without a subcommand, then just run the dangerkotlin execute
    if (currentContext.invokedSubcommand == null) {
      DangerKotlin.run()
    }
  }
}

// TODO: This is currently setup as a basic pass through to the underlying danger-js process but
//  this leaves the terminal documentation to be desired. Expand these Clikt commands to parse the
//  allowed arguments/parameters and then pass them through.
class DangerJsCommand(private val command: Command) : CliktCommand(name = command.argument) {
  override val treatUnknownOptionsAsArgs: Boolean = true

  private val arguments by argument().multiple()

  override fun help(context: Context): String = command.description

  override fun run() {
    DangerJS.process(
      command,
      PROCESS_DANGER_KOTLIN,
      arguments.map { if (it.contains(" ")) "'$it'" else it },
    )
  }
}

class RunnerCommand : CliktCommand(name = "runner") {

  override val treatUnknownOptionsAsArgs: Boolean = true

  private val arguments by argument().multiple()

  override fun help(context: Context): String = Command.RUNNER.description

  override fun run() {
    DangerKotlin.run()
  }
}

fun main(args: Array<String>) {
  DangerCommand(args)
    .subcommands(
      DangerJsCommand(Command.CI),
      DangerJsCommand(Command.LOCAL),
      DangerJsCommand(Command.PR),
      RunnerCommand(),
    )
    .main(args)
}
