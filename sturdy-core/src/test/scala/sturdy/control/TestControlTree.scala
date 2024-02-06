package sturdy.control

import ControlTree.*
import org.scalatest.funsuite.AnyFunSuite
import sturdy.control.EdgeType.{BlockPair, CF}
import sturdy.control.Node.BlockEnd

import scala.annotation.targetName

class TestControlTree extends AnyFunSuite {

  val controlTreeGraphBuilder: ControlTreeGraphBuilder[String, String, String] = ControlTreeGraphBuilder()

  type CT = ControlTree[String, String, String]

  inline def testGraph[A, S, E](name: String)( f : => (ControlTree[A, S, E], Set[Edge[A, S]])): Unit =
    test(name) {
      val graphBuilder = new ControlTreeGraphBuilder[A, S, E]
      val (ct, expected) = f
      val actual = graphBuilder.build(ct)

      val edgesMissing = expected.diff(actual)
      val edgesUnexpected = actual.diff(expected)

      (edgesMissing.isEmpty, edgesUnexpected.isEmpty) match
        case (true, true) => println(ControlGraph.toGraphViz(actual))
        case (false, true) => throw new Exception(s"Missing expected edges : $edgesMissing")
        case (true, false) => throw new Exception(s"Unexpected edges : $edgesUnexpected")
        case (false, false) => throw new Exception(s"Missing and unexpected edges\n\tMissing edges : $edgesMissing\n\tUnexpected edges : $edgesUnexpected")

    }

  private def pairsToEdges[A, S](edgeType: EdgeType, pairs : Set[(Node[A, S], Node[A, S])]) : Set[Edge[A, S]] =
    pairs.map((a, b) => Edge(a, b, edgeType))

  private case class AtomicPair(t: Atomic[String, String, String], g: Node.Atomic[String, String])
  private case class SectionPair(t: Section[String, String, String], gIn: Node.BlockStart[String, String], gOut: Node.BlockEnd[String, String])

  // Creates a ControlTree node and a GraphNode
  private def createAtomic(atom : String) : AtomicPair = AtomicPair(Atomic(atom), Node.Atomic(atom))

  private def createSection(section : String, body: CT) : SectionPair =
    SectionPair(Section(section, body), Node.BlockStart(section), Node.BlockEnd(section))

  testGraph("Atoms") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val ct = a1.t + a2.t + a3.t + a4.t

    (ct, pairsToEdges(CF, Set(
      a1.g -> a2.g,
      a2.g -> a3.g,
      a3.g -> a4.g)))
  }

  testGraph("Empty in a sequence") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")

    val ct = a1.t + Empty() + Empty() + a2.t

    (ct, pairsToEdges(CF, Set(
      a1.g -> a2.g)))
  }

  testGraph("Section") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val sec1 = createSection("sec1", a1.t + a2.t)

    (sec1.t,
      pairsToEdges(CF, Set(
        sec1.gIn -> a1.g,
        a1.g -> a2.g,
        a2.g -> sec1.gOut)) ++
        pairsToEdges(BlockPair, Set(
          sec1.gIn -> sec1.gOut
        ))
    )
  }

  testGraph("Nested sections") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")


    val sec1 = createSection("sec1", a2.t + a3.t)
    val sec2 = createSection("sec2", a1.t + sec1.t + a4.t)

    (sec2.t,
      pairsToEdges(CF, Set(
        sec2.gIn -> a1.g,
        a1.g -> sec1.gIn,
        sec1.gIn -> a2.g,
        a2.g -> a3.g,
        a3.g -> sec1.gOut,
        sec1.gOut -> a4.g,
        a4.g -> sec2.gOut)) ++
      pairsToEdges(BlockPair, Set(
        sec1.gIn -> sec1.gOut,
        sec2.gIn -> sec2.gOut))
    )
  }

  testGraph("Empty section") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")

    val sec1 = createSection("sec1", Empty())
    val sec2 = createSection("sec2", a1.t + sec1.t + a2.t)


    (sec2.t,
      pairsToEdges(CF, Set(
        sec2.gIn -> a1.g,
        a1.g -> sec1.gIn,
        sec1.gIn -> sec1.gOut,
        sec1.gOut -> a2.g,
        a2.g -> sec2.gOut)) ++
        pairsToEdges(BlockPair, Set(
          sec2.gIn -> sec2.gOut))
    )
  }

  testGraph("Fork") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")
    val a5 = createAtomic("A5")

    val secMain = createSection("if", a1.t + Fork(a2.t + a3.t, a4.t) + a5.t)

    (secMain.t,
      pairsToEdges(CF, Set(
       secMain.gIn -> a1.g,
        a1.g -> a2.g,
        a1.g -> a4.g,
        a2.g -> a3.g,
        a4.g -> a5.g,
        a3.g -> a5.g,
        a5.g -> secMain.gOut
      )) ++ pairsToEdges(BlockPair, Set(
        secMain.gIn -> secMain.gOut,
      )))
  }

  testGraph("Fork with empty branch") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")

    val secMain = createSection("if", a1.t + Fork(a2.t, Empty()) + a3.t)

    (secMain.t,
      pairsToEdges(CF, Set(
       secMain.gIn -> a1.g,
        a1.g -> a2.g,
        a2.g -> a3.g,
        a1.g -> a3.g,
        a3.g -> secMain.gOut
      )) ++ pairsToEdges(BlockPair, Set(
        secMain.gIn -> secMain.gOut,
      )))
  }

  testGraph("Nested Fork") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")
    val a5 = createAtomic("A5")
    val a6 = createAtomic("A6")


    val if2 = createSection("If(...) 2", Fork(a4.t, a5.t))
    val if1 = createSection("If(...) 1", Fork(a2.t + a3.t, if2.t))
    val secMain = createSection("secMain", a1.t + if1.t + a6.t)

    (secMain.t,
      pairsToEdges(CF, Set(
        secMain.gIn -> a1.g,
        a1.g -> if1.gIn,
        if1.gIn -> a2.g,
        if1.gIn -> if2.gIn,
        if2.gIn -> a4.g,
        if2.gIn -> a5.g,
        a4.g -> if2.gOut,
        a5.g -> if2.gOut,
        if2.gOut -> if1.gOut,
        a2.g -> a3.g,
        a3.g -> if1.gOut,
        if1.gOut -> a6.g,
        a6.g -> secMain.gOut
      )) ++ pairsToEdges(BlockPair, Set(
        if2.gIn -> if2.gOut,
        if1.gIn -> if1.gOut,
        secMain.gIn -> secMain.gOut
      )))
  }
}