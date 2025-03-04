package systems.danger.kotlin.rules

import kotlinx.coroutines.runBlocking
import systems.danger.kotlin.models.danger.DangerDSL
import systems.danger.kotlin.warn

object RuleManager {

  var debug: Boolean = false

  private val rules = mutableMapOf<String, Rule>()
  private val ruleGraph = RuleGraph()

  internal fun register(rule: Rule) {
    if (rules.containsKey(rule.id))
      error("Rules must have unique ids. ${rule.id} has already been added.")
    rules[rule.id] = rule
    ruleGraph.add(rule)
  }

  internal fun run(dangerDSL: DangerDSL) = runBlocking {
    ruleGraph.forEach { vertex ->
      if (debug) {
        println("Evaluating Rule[${vertex.ruleId}]")
      }
      val result = rules[vertex.ruleId]?.run?.invoke(RuleContext(dangerDSL, this@runBlocking))
      if (result == RuleResult.Exit) {
        if (debug) {
          warn("Rule[${vertex.ruleId}] exited early. No more rules will be run in this session.")
        }
        return@runBlocking
      }
    }
  }
}

/**
 * Apply all registered [Rule] in the rule chain to the current [DangerDSL] context in the order
 * that they are registered and depend on.
 *
 * For example, you can register a rule like so:
 *
 * ```
 * rule("pr-title-check") {
 *   if (!github.pullRequest.title.matches(TITLE_REGEX)) {
 *     fail("Make sure your pull request title matches the format \"JIRA-000: Description of changes\")
 *   }
 *
 *   RuleResult.Continue
 * }
 * ```
 *
 * or if you need a rule to run after another, one that might exit the rule chain early
 *
 * ```
 * rule("user-bot-check") {
 *   if (github.pullRequest.user.type == GitHubUserType.BOT) {
 *     RuleResult.Exit
 *   } else {
 *     RuleResult.Continue
 *   }
 * }
 *
 * rule(id = "pr-summary-validation", "user-bot-check") {
 *   // Do stuff
 *   RuleResult.Continue
 * }
 * ```
 *
 * @see Rule
 * @see rule
 * @see RuleResult
 */
fun DangerDSL.applyRules() = RuleManager.run(this)
