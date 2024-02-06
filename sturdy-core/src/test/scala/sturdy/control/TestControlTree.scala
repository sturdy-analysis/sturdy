package sturdy.control

import ControlTree.*

import org.scalatest.funsuite.AnyFunSuite

class TestControlTree extends AnyFunSuite {

  val controlTreeGraphBuilder: ControlTreeGraphBuilder[String, String] = ControlTreeGraphBuilder()

  private def listToSeq[Atom, Sec](xs : List[ControlTree[Atom, Sec]]) : ControlTree[Atom, Sec] =
    xs.reduceRight { (a, b) => Seq(a, b) }


  test("Fork") {
    val tree = Seq(
      Atomic("a1"),
      Section("main",
        Seq(
          Atomic("a2"),
          Seq(
            Atomic("a3"),
            Seq(
              Fork(
                Atomic("a4"),
                Seq(
                  Atomic("a5"),
                  Atomic("a6")
                )
              ),
              Atomic("a7")
            )))))
    val graph = controlTreeGraphBuilder.rec(tree)
    println(ControlGraph.toGraphViz(graph))
  }

  test("Failure") {
    val tree = Seq(
      Atomic("a1"),
      Section("main",
        Seq(
          Atomic("a2"),
          Seq(
            Atomic("a3"),
            Seq(
              Fork(
                Atomic("a4"),
                Seq(
                  Atomic("a5"),
                  Failed()
                )
              ),
              Atomic("3")
            )))))
    val graph = controlTreeGraphBuilder.rec(tree)
    println(ControlGraph.toGraphViz(graph))
  }

}