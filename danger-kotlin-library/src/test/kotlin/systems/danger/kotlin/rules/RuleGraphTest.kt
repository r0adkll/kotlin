package systems.danger.kotlin.rules

import org.junit.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA
import strikt.assertions.isFailure
import strikt.assertions.message
import systems.danger.kotlin.rules.RuleGraph.Vertex.Companion.Root

class RuleGraphTest {

  private val graph = RuleGraph()
  private val nodeTraveler = NodeTraveler()

  @Test
  fun `Rules without depends execute in order added`() {
    // given
    graph.add(createRule("0"))
    graph.add(createRule("1"))
    graph.add(createRule("2"))

    // when
    graph.forEach(nodeTraveler)

    // then
    expectThat(nodeTraveler.visited).containsExactly(Root.ruleId, "0", "1", "2")
  }

  @Test
  fun `Rules with dependencies traverse as expected`() {
    // given
    graph.add(createRule("0"))
    graph.add(createRule(id = "1", "0", "3"))
    graph.add(createRule(id = "2", "0"))
    graph.add(createRule(id = "3"))

    // when
    graph.forEach(nodeTraveler)

    // then
    expectThat(nodeTraveler.visited).containsExactly(Root.ruleId, "0", "3", "2", "1")
  }

  @Test
  fun `Circular dependency throws exception`() {
    // given
    graph.add(createRule(id = "0", "3"))
    graph.add(createRule(id = "1", "0"))
    graph.add(createRule(id = "2", "1"))
    graph.add(createRule(id = "3", "2"))

    // when/then
    expectCatching { graph.forEach(nodeTraveler) }
      .isFailure()
      .isA<CircularDependencyException>()
      .message
      .get { println(this) }
  }

  private fun createRule(id: String, vararg dependsOn: String) =
    Rule(id, dependsOn.toList()) { RuleResult.Continue }
}

internal class NodeTraveler : (RuleGraph.Vertex) -> Unit {

  val visited = mutableListOf<String>()

  override fun invoke(vertex: RuleGraph.Vertex) {
    visited += vertex.ruleId
  }
}
