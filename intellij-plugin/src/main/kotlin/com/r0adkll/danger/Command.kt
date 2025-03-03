package com.r0adkll.danger

sealed interface Command {
  val option: CommandOption

  data class Local(
    val base: String,
    val useStagedChanges: Boolean = false,
    val numCommits: Int = 0,
  ) : Command {
    override val option: CommandOption = CommandOption.LOCAL
  }

  data class PR(val url: String) : Command {
    override val option: CommandOption = CommandOption.PR
  }
}

enum class CommandOption {
  LOCAL,
  PR,
}
