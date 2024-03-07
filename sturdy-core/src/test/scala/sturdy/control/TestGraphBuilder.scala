package sturdy.control

import org.scalatest.funsuite.AnyFunSuite
import BasicControlEvent.*
import ExceptionControlEvent.*
import BranchingControlEvent.*
import FixpointControlEvent.*

class TestGraphBuilder extends AnyFunSuite {

  inline def testGraph[A, S, E, F](name: String, code: String, es: List[ControlEvent], expected: Set[Edge[A, S]]): Unit =
    test(name) {
      val graphBuilder = new ControlEventGraphBuilder[A, S, E, F]
      es.foreach(graphBuilder.handle)

      val edgesMissing = expected.diff(graphBuilder.edges)
      val edgesUnexpected = graphBuilder.edges.diff(expected)

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
      Atomic("x = input"),
      Atomic("y = x + 1"),
      Atomic("z = input"),
      Atomic("output z"),
    ),
    linearPath(List(
      Node.Atomic("x = input"),
      Node.Atomic("y = x + 1"),
      Node.Atomic("z = input"),
      Node.Atomic("output z")))
  )

  testGraph("Blocks",
    s"""
       |main() {
       |  output 3;
       |  return 0;
       |}
       |""".stripMargin,
    List(
      BeginSection("Call(main)"),
      BeginSection("main"),
      Atomic("output 3"),
      EndSection(),
      EndSection(),
    ),
    linearPath(List(
      Node.BlockStart("Call(main)"),
      Node.BlockStart("main"),
      Node.Atomic("output 3"),
      Node.BlockEnd("main"),
      Node.BlockEnd("Call(main)")))
      ++ Set(
      Edge(Node.BlockStart("main"), Node.BlockEnd("main"), EdgeType.BlockPair),
      Edge(Node.BlockStart("Call(main)"), Node.BlockEnd("Call(main)"), EdgeType.BlockPair),
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
      BeginSection("Call(main)"),
      BeginSection("main"),
      Atomic("output 3"),
      BeginSection("Call(foo)"),
      BeginSection("foo"),
      EndSection(),
      EndSection(),
      Atomic("x = foo()"),
      Atomic("output x"),
      EndSection(),
      EndSection(),
    ),
    linearPath(List(
      Node.BlockStart("Call(main)"),
      Node.BlockStart("main"),
      Node.Atomic("output 3"),
      Node.BlockStart("Call(foo)"),
      Node.BlockStart("foo"),
      Node.BlockEnd("foo"),
      Node.BlockEnd("Call(foo)"),
      Node.Atomic("x = foo()"),
      Node.Atomic("output x"),
      Node.BlockEnd("main"),
      Node.BlockEnd("Call(main)")))
      ++ Set(
      Edge(Node.BlockStart("main"), Node.BlockEnd("main"), EdgeType.BlockPair),
      Edge(Node.BlockStart("Call(main)"), Node.BlockEnd("Call(main)"), EdgeType.BlockPair),
      // Edge(Node.BlockStart("foo"), Node.BlockEnd("foo"), EdgeType.BlockPair), No BlockPair edge if there is a direct CF edge between the two
      Edge(Node.BlockStart("Call(foo)"), Node.BlockEnd("Call(foo)"), EdgeType.BlockPair),
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
      BeginSection("Call(main)"),
      BeginSection("main"),
      Atomic("x = input"),
      Atomic("if(x)"),
      Fork(),
      Atomic("output 0"),
      Switch(),
      Atomic("output 1"),
      Join(),
      EndSection(),
      EndSection(),
    ),
    linearPath(List(
      Node.BlockStart("Call(main)"),
      Node.BlockStart("main"),
      Node.Atomic("x = input"),
      Node.Atomic("if(x)"),
      Node.Atomic("output 0"),
      Node.BlockEnd("main"),
      Node.BlockEnd("Call(main)")
    ))
      ++ Set(
      Edge(Node.Atomic("if(x)"), Node.Atomic("output 1"), EdgeType.CF), // Second part of the Fork
      Edge(Node.Atomic("output 1"), Node.BlockEnd("main"), EdgeType.CF), // Second part of the Fork
      Edge(Node.BlockStart("main"), Node.BlockEnd("main"), EdgeType.BlockPair),
      Edge(Node.BlockStart("Call(main)"), Node.BlockEnd("Call(main)"), EdgeType.BlockPair),
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
      BeginSection("Call(main)"),
      BeginSection("main"),
      Atomic("x = input"),
      Atomic("if(x)"),
      Fork(),
      Atomic("output 0"),
      Atomic("if(x>10)"),
      Fork(),
      Atomic("output 2"),
      Switch(),
      Join(),
      Switch(),
      Atomic("output 1"),
      Join(),
      EndSection(),
      EndSection(),
    ),
    linearPath(List(
      Node.BlockStart("Call(main)"),
      Node.BlockStart("main"),
      Node.Atomic("x = input"),
      Node.Atomic("if(x)"),
      Node.Atomic("output 0"),
      Node.Atomic("if(x>10)"),
      Node.Atomic("output 2"),
      Node.BlockEnd("main"),
      Node.BlockEnd("Call(main)")
    ))
      ++ Set(
      Edge(Node.Atomic("if(x)"), Node.Atomic("output 1"), EdgeType.CF), // Second part of the Fork
      Edge(Node.Atomic("output 1"), Node.BlockEnd("main"), EdgeType.CF), // Second part of the Fork
      Edge(Node.Atomic("if(x>10)"), Node.BlockEnd("main"), EdgeType.CF), // Second part of the nested Fork
      Edge(Node.BlockStart("main"), Node.BlockEnd("main"), EdgeType.BlockPair),
      Edge(Node.BlockStart("Call(main)"), Node.BlockEnd("Call(main)"), EdgeType.BlockPair),
    )
  )

}

