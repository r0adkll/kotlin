package systems.danger.cmd.dangerjs

import systems.danger.Log
import systems.danger.cmd.Command
import systems.danger.cmd.exec

object DangerJS : DangerJSBridge {

  override fun process(command: Command, processName: String, args: List<String>) {
    Log.info("Launching Danger-JS", verbose = true)

    val dangerJSArgumentIndex = args.indexOf("--danger-js-path")
    val dangerJSPath: String =
      if (dangerJSArgumentIndex != -1 && args.count() > dangerJSArgumentIndex + 1) {
        args[dangerJSArgumentIndex + 1]
      } else {
        "$(which danger)"
      }

    exec(dangerJSPath) {
      arguments(
        command.argument,
        "--verbose",
        "--process",
        processName,
        "--passURLForDSL",
        *args.toTypedArray(),
      )
    }
  }
}
