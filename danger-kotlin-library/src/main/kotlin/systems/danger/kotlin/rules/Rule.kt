package systems.danger.kotlin.rules

import kotlinx.coroutines.CoroutineScope
import systems.danger.kotlin.models.danger.DangerDSL

class RuleContext(
  private val danger: DangerDSL,
  private val scope: CoroutineScope,
): DangerDSL by danger, CoroutineScope by scope

internal class Rule(
  val id: String,
  val dependsOn: List<String>,
  val run: suspend RuleContext.() -> RuleResult,
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
 * Register a new Danger rule to be executed in a specific order while also being able to interrupt
 * the chain of rules with early exists. For example, if the PR was made by a bot you may not want
 * to evaluate the remaining rules in your chain.
 *
 * @param id the unique identifier of the rule to register. These must be unique for a given execution.
 * @param dependsOn a list of rules that this rule depends on. These must execute before this one is allowed to.
 * @param block the rule block to execute when [applyRules] is called
 */
fun rule(id: String, vararg dependsOn: String, block: suspend RuleContext.() -> RuleResult) {
  val newRule = Rule(id, dependsOn.toList(), block)
  RuleManager.register(newRule)
}

/**
 * The result of running a rule so that the manager can determine whether to stop evaluating future rules
 * or to continue.
 */
sealed interface RuleResult {
  /**
   * Return this to continue evaluating rules in this workflow
   */
  data object Continue : RuleResult

  /**
   * Return this to stop evaluating all rules in this workflow
   */
  data object Exit : RuleResult
}
