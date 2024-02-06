package sturdy.control

import ControlTree.*
import org.scalatest.funsuite.AnyFunSuite

import scala.annotation.targetName

class TestControlTree extends AnyFunSuite {

  val controlTreeGraphBuilder: ControlTreeGraphBuilder[String, String] = ControlTreeGraphBuilder()


  test("Fork") {
    val tree =
      Atomic("a1") +
      Section("main",
          Atomic("a2") + Atomic("a3") + Fork(
            Atomic("a4"),
            Atomic("a5") + Atomic("a6")
          )
      ) + Atomic("a7")
    val graph = controlTreeGraphBuilder.rec(tree)
    println(ControlGraph.toGraphViz(graph))
  }

  test("Failure") {
    val tree = Section("main",
          Atomic("a2") + Atomic("a3") + Fork(
                Atomic("a4"),
                Atomic("a5") + Atomic("a6") + Failed())
              ) + Atomic("3")
    val graph = controlTreeGraphBuilder.rec(tree)
    println(ControlGraph.toGraphViz(graph))
  }

  test("Multiple failures") {
    val tree = Section("main",
      Atomic("a2") + Atomic("a3") + Fork(
        Atomic("a4") + Fork(
          Atomic("a8"),
          Atomic("a9") + Failed()
        ),
        Atomic("a5") + Atomic("a6") + Failed())
    ) + Atomic("3")
    val graph = controlTreeGraphBuilder.rec(tree)
    println(ControlGraph.toGraphViz(graph))
  }

}