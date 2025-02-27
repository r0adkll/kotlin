package systems.danger.cmd

enum class Command(val argument: String) {
  CI("ci"),
  LOCAL("local"),
  PR("pr"),
  RUNNER("runner");

  val description: String
    get() {
      return when (this) {
        CI -> "Use this on CI"
        LOCAL -> "Use this to run danger against your local changes from master/main"
        PR -> "Run danger-kotlin locally against a PR"
        RUNNER -> "Triggers the Dangerfile evaluation (used mainly by DangerJS)"
      }
    }
}
