package systems.danger.kotlin.rules

import systems.danger.kotlin.rules.RuleGraph.Edge

internal class RuleGraph : Iterable<RuleGraph.Vertex> {

  @JvmInline
  value class Vertex(val ruleId: String) {
    companion object {
      val Root = Vertex("root")
    }
  }

  data class Edge(val src: Vertex, val dest: Vertex)

  private val vertexMap = mutableMapOf<Vertex, MutableList<Edge>>()

  fun add(rule: Rule): Vertex {
    val vertex = Vertex(rule.id)

    // An edge might have been added before the rule was, so lets make sure
    // we don't overwrite existing edges
    if (vertexMap[vertex] == null) {
      vertexMap[vertex] = mutableListOf()
    }

    if (rule.dependsOn.isEmpty()) {
      addDirectedEdge(Vertex.Root, Vertex(rule.id))
    } else {
      rule.dependsOn.forEach { dependsOnRuleId ->
        addDirectedEdge(
          // The rule this one depends on would be the source vertex
          Vertex(dependsOnRuleId),

          // This rule itself, would be the destination vertex
          Vertex(rule.id),
        )
      }
    }

    return vertex
  }

  private fun addDirectedEdge(src: Vertex, dest: Vertex) {
    val edge = Edge(src, dest)
    if (vertexMap.containsKey(src)) {
      vertexMap[src]?.add(edge)
    } else {
      // If the src vertex rule hasn't been added
      vertexMap[src] = mutableListOf(edge)
    }
  }

  /**
   * Traverse this graph using a topological sorting algorithm to make sure we visit the nodes in
   * correct order per their dependency setup.
   *
   * [Kahn's algorithm](https://en.wikipedia.org/wiki/Topological_sorting#Algorithms)
   */
  private fun sortTopologically(): List<Vertex> {
    // Create a copy of our edge map to manipulate
    val graph = vertexMap.toMutableMap()

    val visited = mutableListOf<Vertex>()
    val queue = ArrayDeque<Vertex>()

    queue.addLast(Vertex.Root)

    while (queue.isNotEmpty()) {
      val current = queue.removeFirst()
      visited += current

      // Create a copy of the edges to iterate through
      val edges = graph[current]?.toList() ?: emptyList()
      for (edge in edges) {
        // Remove the edge
        graph[current]?.remove(edge)

        // if [edge.dest] has no more incoming edges, add to queue to be visited
        if (graph.flatMap { it.value }.none { it.dest == edge.dest }) {
          queue.addLast(edge.dest)
        }
      }
    }

    val remainingEdges = graph.filter { it.value.isNotEmpty() }
    if (remainingEdges.isNotEmpty()) {
      throw CircularDependencyException(remainingEdges)
    }

    return visited
  }

  override fun iterator(): Iterator<Vertex> {
    return sortTopologically().iterator()
  }
}

internal data class CircularDependencyException(
  val remainingEdges: Map<RuleGraph.Vertex, MutableList<Edge>>
) :
  RuntimeException(
    buildString {
      appendLine("This graph has a circular dependency:")
      appendLine()

      val edges = remainingEdges.flatMap { it.value }
      val edgeMap = edges.associateBy { it.src }.toMutableMap()

      val edgeChain = mutableListOf<RuleGraph.Vertex>()
      val edgeQueue = ArrayDeque<Edge>()
      val firstEdge = edges.first()
      edgeQueue.addLast(firstEdge)
      edgeMap.remove(firstEdge.src)
      edgeChain.add(firstEdge.src)

      while (edgeQueue.isNotEmpty()) {
        val edge = edgeQueue.removeFirst()
        edgeChain.add(edge.dest)

        val nextEdge = edgeMap[edge.dest]
        if (nextEdge != null) {
          edgeMap.remove(edge.dest)
          edgeQueue.addLast(nextEdge)
        }
      }

      append("\t").appendLine(edgeChain.joinToString(" --> ") { it.ruleId })
    }
  )
