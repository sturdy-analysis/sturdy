package sturdy.control

import org.scalatest.funsuite.AnyFunSuite
import BasicControlEvent.*
import ExceptionControlEvent.*
import BranchingControlEvent.*
import FixpointControlEvent.*

class TestGraphBuilder extends AnyFunSuite {

  inline def testGraph[A, S, E, F](name: String, code: String, es: List[ControlEvent[A,S,E,F]], expected: Set[Edge[A, S]]): Unit =
    test(name) {
      val graphBuilder = new ControlEventGraphBuilder[A, S, E, F]
      es.foreach(graphBuilder.handle)

      val edges = graphBuilder.get.edges
      val edgesMissing = expected.diff(edges)
      val edgesUnexpected = edges.diff(expected)

      (edgesMissing.isEmpty, edgesUnexpected.isEmpty) match
        case (true, true) => ()
        case (false, true) => throw new Exception(s"Missing expected edges : ${edgesMissing}")
        case (true, false) => throw new Exception(s"Unexpected edges : ${edgesUnexpected}")
        case (false, false) => throw new Exception(s"Missing and unexpected edges\n\tMissing edges : ${edgesMissing}\n\tUnexpected edges : ${edgesUnexpected}")
    }
    ()

  private def linearPath[A, S](nodes: List[Node[A, S]]): Set[Edge[A, S]] =
    nodes.sliding(2).map {
      case Seq(a, b) => Edge(a, b, EdgeType.CF)
    }.toSet


  testGraph("Atoms",
    s"""
       |var x,y,z;
       |x = input;
       |y = x + 1;
       |z = input;
       |output z;
       |""".stripMargin,
    List(
      atomic("x = input"),
      atomic("y = x + 1"),
      atomic("z = input"),
      atomic("output z"),
    ),
    linearPath(List(
      Node.atomic("x = input"),
      Node.atomic("y = x + 1"),
      Node.atomic("z = input"),
      Node.atomic("output z")))
  )

  testGraph("Blocks",
    s"""
       |main() {
       |  output 3;
       |  return 0;
       |}
       |""".stripMargin,
    List(
      beginSection("Call(main)"),
      beginSection("main"),
      atomic("output 3"),
      EndSection(),
      EndSection(),
    ),
    linearPath(List(
      Node.blockStart("Call(main)"),
      Node.blockStart("main"),
      Node.atomic("output 3"),
      Node.blockEnd("main"),
      Node.blockEnd("Call(main)")))
      ++ Set(
      Edge(Node.blockStart("main"), Node.blockEnd("main"), EdgeType.BlockPair),
      Edge(Node.blockStart("Call(main)"), Node.blockEnd("Call(main)"), EdgeType.BlockPair),
    )
  )

  testGraph("Nested blocks (call)",
    s"""
       |main() {
       |  var x;
       |  output 3;
       |  x = foo()
       |  output x;
       |  return 0;
       |}
       |
       |foo() {
       |  return 5;
       |}
       |""".stripMargin,
    List(
      beginSection("Call(main)"),
      beginSection("main"),
      atomic("output 3"),
      beginSection("Call(foo)"),
      beginSection("foo"),
      EndSection(),
      EndSection(),
      atomic("x = foo()"),
      atomic("output x"),
      EndSection(),
      EndSection(),
    ),
    linearPath(List(
      Node.blockStart("Call(main)"),
      Node.blockStart("main"),
      Node.atomic("output 3"),
      Node.blockStart("Call(foo)"),
      Node.blockStart("foo"),
      Node.blockEnd("foo"),
      Node.blockEnd("Call(foo)"),
      Node.atomic("x = foo()"),
      Node.atomic("output x"),
      Node.blockEnd("main"),
      Node.blockEnd("Call(main)")))
      ++ Set(
      Edge(Node.blockStart("main"), Node.blockEnd("main"), EdgeType.BlockPair),
      Edge(Node.blockStart("Call(main)"), Node.blockEnd("Call(main)"), EdgeType.BlockPair),
      // Edge(Node.blockStart("foo"), Node.blockEnd("foo"), EdgeType.BlockPair), No BlockPair edge if there is a direct CF edge between the two
      Edge(Node.blockStart("Call(foo)"), Node.blockEnd("Call(foo)"), EdgeType.BlockPair),
    )
  )

  testGraph("Fork",
    s"""
       |main() {
       |  val x;
       |  x = input;
       |  if(x) {
       |    output 0;
       |  }
       |  else {
       |    output 1;
       |  }
       |  return 0;
       |}
       |""".stripMargin,
    List(
      beginSection("Call(main)"),
      beginSection("main"),
      atomic("x = input"),
      atomic("if(x)"),
      Fork(),
      atomic("output 0"),
      Switch(),
      atomic("output 1"),
      Join(),
      EndSection(),
      EndSection(),
    ),
    linearPath(List(
      Node.blockStart("Call(main)"),
      Node.blockStart("main"),
      Node.atomic("x = input"),
      Node.atomic("if(x)"),
      Node.atomic("output 0"),
      Node.blockEnd("main"),
      Node.blockEnd("Call(main)")
    ))
      ++ Set(
      Edge(Node.atomic("if(x)"), Node.atomic("output 1"), EdgeType.CF), // Second part of the Fork
      Edge(Node.atomic("output 1"), Node.blockEnd("main"), EdgeType.CF), // Second part of the Fork
      Edge(Node.blockStart("main"), Node.blockEnd("main"), EdgeType.BlockPair),
      Edge(Node.blockStart("Call(main)"), Node.blockEnd("Call(main)"), EdgeType.BlockPair),
    )
  )

  testGraph("Nested fork with empty then",
    s"""
       |main() {
       |  val x;
       |  x = input;
       |  if(x) {
       |    output 0;
       |    if(x>10) {
       |      output 2;
       |    }
       |  }
       |  else {
       |    output 1;
       |  }
       |  return 0;
       |}
       |""".stripMargin,
    List(
      beginSection("Call(main)"),
      beginSection("main"),
      atomic("x = input"),
      atomic("if(x)"),
      Fork(),
      atomic("output 0"),
      atomic("if(x>10)"),
      Fork(),
      atomic("output 2"),
      Switch(),
      Join(),
      Switch(),
      atomic("output 1"),
      Join(),
      EndSection(),
      EndSection(),
    ),
    linearPath(List(
      Node.blockStart("Call(main)"),
      Node.blockStart("main"),
      Node.atomic("x = input"),
      Node.atomic("if(x)"),
      Node.atomic("output 0"),
      Node.atomic("if(x>10)"),
      Node.atomic("output 2"),
      Node.blockEnd("main"),
      Node.blockEnd("Call(main)")
    ))
      ++ Set(
      Edge(Node.atomic("if(x)"), Node.atomic("output 1"), EdgeType.CF), // Second part of the Fork
      Edge(Node.atomic("output 1"), Node.blockEnd("main"), EdgeType.CF), // Second part of the Fork
      Edge(Node.atomic("if(x>10)"), Node.blockEnd("main"), EdgeType.CF), // Second part of the nested Fork
      Edge(Node.blockStart("main"), Node.blockEnd("main"), EdgeType.BlockPair),
      Edge(Node.blockStart("Call(main)"), Node.blockEnd("Call(main)"), EdgeType.BlockPair),
    )
  )

}

