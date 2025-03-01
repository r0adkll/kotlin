package systems.danger.cmd

import platform.posix.*
import systems.danger.Log

class Cmd internal constructor(private val name: String) {
  var arguments: Array<out String> = emptyArray()

  fun arguments(vararg arg: String) {
    arguments = arg
  }

  internal fun exec() {
    "$name ${arguments.joinToString(" ")}"
      .apply { Log.info("Executing $this - pid ${getpid()}") }
      .also {
        val exitCode = system(it)

        if (exitCode != 0) {
          throw Exception("Command $it exited with code $exitCode")
        }
      }
  }
}

fun exec(name: String, builder: Cmd.() -> Unit) {
  val cmd = Cmd(name).apply(builder)
  cmd.exec()
}
