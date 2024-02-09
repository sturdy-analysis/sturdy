package sturdy.control

import org.scalatest.funsuite.AnyFunSuite
import sturdy.control.ControlTree.*
import sturdy.control.EdgeType.{BlockPair, CF}
import sturdy.control.Node.BlockEnd
import sturdy.data.combineMaps

import scala.annotation.targetName

class TestControlTreeGraphBuilder extends AnyFunSuite {

  type CT = ControlTree[String, String, String]
  type AtomPair = (ControlTree[String, String, String], Node[String, String])
  val controlTreeGraphBuilder: ControlTreeGraphBuilder[String, String, String] = ControlTreeGraphBuilder()
  private val nFailure: AtomPair = (Failed(), Node.Failure[String, String]())

  inline def testGraph[A, S, E](name: String)(f: => (ControlTree[A, S, E], Set[Edge[A, S]])): Unit =
    test(name) {
      val graphBuilder = new ControlTreeGraphBuilder[A, S, E]
      val (ct, expected) = f
      val actual = graphBuilder.build(ct)

      val edgesMissing = expected.diff(actual)
      val edgesUnexpected = actual.diff(expected)

      println(ControlGraph.toGraphViz(actual))

      (edgesMissing.isEmpty, edgesUnexpected.isEmpty) match
        case (true, true) => ()
        case (false, true) => throw new Exception(s"Missing expected edges : $edgesMissing")
        case (true, false) => throw new Exception(s"Unexpected edges : $edgesUnexpected")
        case (false, false) => throw new Exception(s"Missing and unexpected edges\n\tMissing edges : $edgesMissing\n\tUnexpected edges : $edgesUnexpected")
    }

  private def pairsToEdges(edgeType: EdgeType, pairs: Set[(Node[String, String], Node[String, String])]): Set[Edge[String, String]] =
    pairs.map((a, b) => Edge(a, b, edgeType))

  private given scala.Conversion[AtomPair, ControlTree[String, String, String]] = _._1

  private given scala.Conversion[(AtomPair, AtomPair), (Node[String, String], Node[String, String])] = (a, b) => (a._2, b._2)

  private given scala.Conversion[SectionPair, Section[String, String, String]] = _.n

  // Creates a ControlTree node and a GraphNode
  private def createAtomic(atom: String): AtomPair = (Atomic(atom), Node.Atomic(atom))

  private def createSection(section: String, body: CT): SectionPair = SectionPair(Section(section, body), (null, Node.BlockStart(section)), (null, Node.BlockEnd(section)))

  case class SectionPair(n: Section[String, String, String], start: AtomPair, end: AtomPair)

  testGraph("Atom sequence") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val ct = a1 + a2 + a3 + a4

    (ct,
      pairsToEdges(CF, Set(
        a1 -> a2,
        a2 -> a3,
        a3 -> a4)))
  }

  testGraph("Empty in a sequence") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")

    val ct = a1 + Empty() + Empty() + a2

    (ct,
      pairsToEdges(CF, Set(
        a1 -> a2)))
  }

  testGraph("Section") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")

    val sec1 = createSection("sec1", a1 + a2)

    (sec1,
      pairsToEdges(CF, Set(
        sec1.start -> a1,
        a1 -> a2,
        a2 -> sec1.end)) ++ pairsToEdges(BlockPair, Set(
        sec1.start -> sec1.end)))
  }

  testGraph("Nested sections") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val sec1 = createSection("sec1", a2 + a3)
    val sec2 = createSection("sec2", a1 + sec1 + a4)
    val sec3 = createSection("sec3", sec2)

    (sec3,
      pairsToEdges(CF, Set(
        sec3.start -> sec2.start,
        sec2.start -> a1,
        a1 -> sec1.start,
        sec1.start -> a2,
        a2 -> a3,
        a3 -> sec1.end,
        sec1.end -> a4,
        a4 -> sec2.end,
        sec2.end -> sec3.end)) ++ pairsToEdges(BlockPair, Set(
        sec1.start -> sec1.end,
        sec2.start -> sec2.end,
        sec3.start -> sec3.end)))
  }

  testGraph("Empty section with no helper BlockPair edge") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")

    val sec1 = createSection("sec1", Empty())
    val sec2 = createSection("sec2", a1 + sec1 + a2)

    (sec2,
      pairsToEdges(CF, Set(
        sec2.start -> a1,
        a1 -> sec1.start,
        sec1.start -> sec1.end,
        sec1.end -> a2,
        a2 -> sec2.end)) ++ pairsToEdges(BlockPair, Set(
        sec2.start -> sec2.end)))
  }

  testGraph("Failure") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")

    val nodes = a1 + a2 + Failed()

    (nodes,
      pairsToEdges(CF, Set(
        a1 -> a2,
        a2 -> nFailure)))
  }

  testGraph("Failure in Section, no end of section") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")

    val sec1 = createSection("sec1", a1 + a2 + Failed())

    (sec1,
      pairsToEdges(CF, Set(
        sec1.start -> a1,
        a1 -> a2,
        a2 -> nFailure)))
  }

  testGraph("Failure in nested section") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")

    val secNotClosed = createSection("secInner", a3 + nFailure)
    val secMain = createSection("secMain", a1 + Fork(a2 + nFailure, secNotClosed))

    (secMain,
      pairsToEdges(CF, Set(
        secMain.start -> a1,
        a1 -> a2,
        a1 -> secNotClosed.start,
        secNotClosed.start -> a3,
        a3 -> nFailure,
        a2 -> nFailure)))
  }

  testGraph("Fork") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")
    val a5 = createAtomic("A5")

    val secMain = createSection("if", a1 + Fork(a2 + a3, a4) + a5)

    (secMain,
      pairsToEdges(CF, Set(
        secMain.start -> a1,
        a1 -> a2,
        a1 -> a4,
        a2 -> a3,
        a4 -> a5,
        a3 -> a5,
        a5 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
        secMain.start -> secMain.end)))
  }

  testGraph("Fork with empty branch") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")

    val secMain = createSection("if", a1 + Fork(a2, Empty()) + a3)

    (secMain,
      pairsToEdges(CF, Set(
        secMain.start -> a1,
        a1 -> a2,
        a2 -> a3,
        a1 -> a3,
        a3 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
        secMain.start -> secMain.end)))
  }

  testGraph("Nested Fork") {
    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")
    val a5 = createAtomic("A5")
    val a6 = createAtomic("A6")

    val if2 = createSection("If(...) 2", Fork(a4, a5))
    val if1 = createSection("If(...) 1", Fork(a2 + a3, if2))
    val secMain = createSection("secMain", a1 + if1 + a6)

    (secMain,
      pairsToEdges(CF, Set(
        secMain.start -> a1,
        a1 -> if1.start,
        if1.start -> a2,
        if1.start -> if2.start,
        if2.start -> a4,
        if2.start -> a5,
        a4 -> if2.end,
        a5 -> if2.end,
        if2.end -> if1.end,
        a2 -> a3,
        a3 -> if1.end,
        if1.end -> a6,
        a6 -> secMain.end))
        ++ pairsToEdges(BlockPair, Set(
        if2.start -> if2.end,
        if1.start -> if1.end,
        secMain.start -> secMain.end)))
  }

  testGraph("Failure in one branch, not preserved after the fork") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val secMain = createSection("secMain", a1 + Fork(a2, a3 + nFailure) + a4)

    (secMain,
      pairsToEdges(CF, Set(
        secMain.start -> a1,
        a1 -> a2,
        a1 -> a3,
        a3 -> nFailure,
        a2 -> a4,
        a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
        secMain.start -> secMain.end)))
  }

  testGraph("Failure in both branches, preserved after fork") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")

    val secMain = createSection("secMain", a1 + Fork(a2 + nFailure, a3 + nFailure))

    (secMain,
      pairsToEdges(CF, Set(
        secMain.start -> a1,
        a1 -> a2,
        a1 -> a3,
        a3 -> nFailure,
        a2 -> nFailure)))
  }

  testGraph("Empty Try") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val secMain = createSection("secMain", a1 + Try(a2 + a3, Map.empty) + a4)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> a2,
      a2 -> a3,
      a3 -> a4,
      a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }

  testGraph("Try with throw and handle") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val secMain = createSection("secMain", a1 + Try(a2 + Throw("Exc1"), Map("Exc1" -> a3)) + a4)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> a2,
      a2 -> a3,
      a3 -> a4,
      a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }

  testGraph("Try with may throw and handle") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val secMain = createSection("secMain", a1 + Try(a2 + Fork(Empty(), Throw("Exc1")), Map("Exc1" -> a3)) + a4)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> a2,
      a2 -> a3,
      a2 -> a4,
      a3 -> a4,
      a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }

  testGraph("Try with nested block, correct dispatch") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val handleOut = createAtomic("Handle outer")
    val handleIn = createAtomic("Handle inner")

    val tryInner = Try(a3 + Fork(Empty(), Throw("Exc1")), Map("Exc1" -> handleIn))
    val tryOuter = Try(a2 + Fork(Empty(), Throw("Exc1")) + tryInner, Map("Exc1" -> handleOut))

    val secMain = createSection("secMain", a1 + tryOuter + a4)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> a2,
      a2 -> a3,
      a2 -> handleOut,
      a3 -> a4,
      a3 -> handleIn,
      handleIn -> a4,
      handleOut -> a4,
      a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }

  testGraph("Try with nested rethrow, correct dispatch") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val handle = createAtomic("Handle Exc1 Throw Exc2 in inner Try")
    val handleIn = createAtomic("Handle Exc2 Inner")
    val handleOut = createAtomic("Handle Exc2 Outer")

    val tryInner = Try(Fork(a1 + Throw("Exc1"), a2 + Throw("Exc2")), Map("Exc1" -> (handle + Throw("Exc2")), "Exc2" -> handleIn))
    val tryOuter = Try(tryInner + a3, Map("Exc2" -> handleOut))

    val secMain = createSection("secMain", tryOuter + a4)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      secMain.start -> a2,
      a1 -> handle,
      handle -> handleOut,
      a2 -> handleIn,
      handleIn -> a3,
      a3 -> a4,
      handleOut -> a4,
      a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }

  testGraph("Try nested, handle outer") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")

    val handle = createAtomic("Handle Exc1")

    val tryInner = Try(a3 + Throw("Exc1"), Map.empty)
    val tryOuter = Try(a2 + tryInner, Map("Exc1" -> handle))

    val secMain = createSection("secMain", a1 + tryOuter)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> a2,
      a2 -> a3,
      a3 -> handle,
      handle -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }

  testGraph("Try with nested block, rethrow") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val handleOut = createAtomic("Handle outer")
    val handleIn = createAtomic("Handle inner")

    val tryInner = Try(a3 + Fork(Empty(), Throw("Exc1")), Map("Exc1" -> (handleIn + Throw("Exc1"))))
    val tryOuter = Try(a2 + Fork(Empty(), Throw("Exc1")) + tryInner, Map("Exc1" -> handleOut))

    val secMain = createSection("secMain", a1 + tryOuter + a4)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> a2,
      a2 -> a3,
      a2 -> handleOut,
      a3 -> a4,
      a3 -> handleIn,
      handleIn -> handleOut,
      handleOut -> a4,
      a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }

  testGraph("Try with multiple throw") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val handle1 = createAtomic("Handle Exc1")
    val handle2 = createAtomic("Handle Exc2")

    val secMain = createSection("secMain", Try(a1 + Fork(a2, Fork(Throw("Exc1"), a3 + Throw("Exc2"))), Map("Exc1" -> handle1, "Exc2" -> handle2)) + a4)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> a2,
      a1 -> handle1,
      a1 -> a3,
      a3 -> handle2,
      a2 -> a4,
      handle1 -> a4,
      handle2 -> a4,
      a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }

  testGraph("Try with failure") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a3 = createAtomic("A3")
    val a4 = createAtomic("A4")

    val secMain = createSection("secMain", a1 + Try(Failed(), Map("Exc1" -> a3)))

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> nFailure)))
  }

  testGraph("Try with failure in handler") {

    val a1 = createAtomic("A1")
    val a2 = createAtomic("A2")
    val a4 = createAtomic("A4")

    val secMain = createSection("secMain", a1 + Try(a2 + Fork(Empty(), Throw("Exc1")), Map("Exc1" -> Failed())) + a4)

    (secMain, pairsToEdges(CF, Set(
      secMain.start -> a1,
      a1 -> a2,
      a2 -> a4,
      a2 -> nFailure,
      a4 -> secMain.end)) ++ pairsToEdges(BlockPair, Set(
      secMain.start -> secMain.end)))
  }


}