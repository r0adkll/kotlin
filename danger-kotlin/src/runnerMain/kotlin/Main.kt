import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.eagerOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import systems.danger.DangerKotlin
import systems.danger.cmd.Command
import systems.danger.cmd.dangerjs.DangerJS

const val PROCESS_DANGER_KOTLIN = "danger-kotlin"
const val VERSION = "1.3.3"

class DangerCommand(private val originalArgv: Array<String>) :
  CliktCommand(name = "danger-kotlin") {

  init {
    eagerOption("--version") { throw PrintMessage("$PROCESS_DANGER_KOTLIN version $VERSION") }
  }

  private val verbose by option("-v", "--verbose").flag()

  override fun run() {
    if (verbose) {
      echo("Starting Danger-Kotlin $VERSION with args '${originalArgv.joinToString(", ")}'")
    }
  }
}

// TODO: This is currently setup as a basic pass through to the underlying danger-js process but
//  this leaves the terminal documentation to be desired. Expand these Clikt commands to parse the
//  allowed arguments/parameters and then pass them through.
class DangerJsCommand(private val command: Command) : CliktCommand(name = command.argument) {

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
