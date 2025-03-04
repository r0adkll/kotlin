package systems.danger.kotlin.rules

import systems.danger.kotlin.models.danger.DangerDSL
import systems.danger.kotlin.warn

object RuleManager {

  var debug: Boolean = false

  private val rules = mutableMapOf<String, Rule>()
  private val ruleGraph = RuleGraph()

  internal fun register(rule: Rule) {
    if (rules.containsKey(rule.id)) error("Rules must have unique ids. ${rule.id} has already been added.")
    rules[rule.id] = rule
    ruleGraph.add(rule)
  }

  fun run(dangerDSL: DangerDSL) {
    ruleGraph.forEach { vertex ->
      val result = rules[vertex.ruleId]?.run?.invoke(dangerDSL)
      if (result == RuleResult.Exit) {
        if (debug) {
          warn("Rule[${vertex.ruleId}] exited early. No more rules will be run in this session.")
        }
        return@run
      }
    }
  }
}

fun DangerDSL.applyRules() = RuleManager.run(this)
