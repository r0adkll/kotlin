package systems.danger.kotlin.rules

import systems.danger.kotlin.models.danger.DangerDSL

class Rule(
  val id: String,
  val dependsOn: List<String>,
  val run: suspend DangerDSL.() -> RuleResult,
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Rule

    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}

/**
 * Register a new Danger rule to be executed in a specific order while also being able to interrupt the chain
 * of rules with early exists. For example, if the PR was made by a bot you may not want to evaluate the
 * remaining rules in your chain.
 */
fun rule(id: String, vararg dependsOn: String, block: suspend DangerDSL.() -> RuleResult) {
  val newRule = Rule(id, dependsOn.toList(), block)
  RuleManager.register(newRule)
}

sealed interface RuleResult {
  data object Continue : RuleResult
  data object Exit : RuleResult
}
