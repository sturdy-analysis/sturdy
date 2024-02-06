package sturdy.control

import ControlTree.*
import org.scalatest.funsuite.AnyFunSuite

import scala.annotation.targetName

class TestControlTree extends AnyFunSuite {

  val controlTreeGraphBuilder: ControlTreeGraphBuilder[String, String, String] = ControlTreeGraphBuilder()

  type CT = ControlTree[String, String, String]

  test("Fork") {
    val tree : CT =
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
    val tree : CT = Section("main",
          Atomic("a2") + Atomic("a3") + Fork(
                Atomic("a4"),
                Atomic("a5") + Atomic("a6") + Failed())
              ) + Atomic("3")
    val graph = controlTreeGraphBuilder.rec(tree)
    println(ControlGraph.toGraphViz(graph))
  }

  test("Multiple failures") {
    val tree : CT = Section("main",
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

  test("Exception") {
    val tree : CT = Section("main",
      Atomic("1") + Try(
        Atomic("2") + Throw("Exc1"),
        Map[String, CT]("Exc1" -> Atomic("Handle Exc1"), "Exc2" -> Atomic("Handle Exc2"))
      )
      + Atomic("5")
    )
    val graph = controlTreeGraphBuilder.rec(tree)
    println(ControlGraph.toGraphViz(graph))
  }
  
  test("Exception") {
    val tree : CT = Section("main",
      Atomic("1") + Try(
        Atomic("2") + Throw("Exc1"),
        Map[String, CT]("Exc1" -> Atomic("Handle Exc1"), "Exc2" -> Atomic("Handle Exc2"))
      )
      + Atomic("5")
    )
    val graph = controlTreeGraphBuilder.rec(tree)
    println(ControlGraph.toGraphViz(graph))
  }

}