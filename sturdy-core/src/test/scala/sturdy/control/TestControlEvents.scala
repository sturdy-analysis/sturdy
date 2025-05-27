package sturdy.control

import org.scalatest.funsuite.AnyFunSuite
import BasicControlEvent.*, ExceptionControlEvent.*, BranchingControlEvent.*, FixpointControlEvent.*

class TestControlEvents extends AnyFunSuite {
  inline def testEvents[A,S,E,F](name: String, code: String, es: List[ControlEvent[A,S,E,F]]): Unit =
    test(name) {
      val rec = new ControlEventChecker[A, S, E, F]()
      es.foreach(rec.handle)
    }

  testEvents("Test 1",
    s"""
     |x = a
     |while (x)
     |  x = x - 1
     |y = x
     |""".stripMargin,
    List(
      atomic("x = a"),
      atomic("while (x)"),
      BeginFixpoint("while"),
      atomic("x = x - 1"),
      atomic("while (x)"),
      Recurrent(true),
      EndFixpoint(),
      Restart(),
      BeginFixpoint("while"),
      atomic("x = x - 1"),
      atomic("while (x)"),
      Recurrent(true),
      EndFixpoint(),
      atomic("y = x")
    )
  )

  testEvents("Test 2",
    s"""  var x;
       |  if (n <= 0)
       |    x = 1
       |  else
       |    x = n * f(n - 1)
       |  ret x""".stripMargin,
    List(
      beginSection("f"),
      BeginFixpoint("fun f"),
      atomic("if"),
      Fork(),
      atomic("x = 1"),
      Switch(),
      beginSection("f"),
      Recurrent(true),
      EndSection(),
      Join(),
      atomic("ret x"),
      EndFixpoint(),

      Restart(),
      BeginFixpoint("fun f"),
      atomic("if"),
      Fork(),
      atomic("x = 1"),
      Switch(),
      atomic("x = n * f(n-1)"),
      Join(),
      atomic("ret x"),
      EndFixpoint(),

      EndSection(),
    )
  )


  testEvents("Test 3",
    s"""  try
       |    x = 1
       |    throw A
       |    x = 2
       |  catch A =>
       |    x = 3
       |  catch B =>
       |    x = 4
       |  ret x""".stripMargin,
    List(
      beginSection("f"),
      BeginTry(),
        atomic("x = 1"),
        Throw("A"),
        Catching(),
        BeginHandle("A"),
          atomic("x = 3"),
        EndHandle(),
      EndTry(),
      EndSection()
    )
  )

  testEvents("Test 4",
    s"""  try
       |    x = 1
       |    throw B
       |    x = 2
       |  catch A =>
       |    x = 3
       |  catch B =>
       |    x = 4
       |  ret x""".stripMargin,
    List(
      beginSection("f"),
      BeginTry(),
        atomic("x = 1"),
        Throw("B"),
        Catching(),
        BeginHandle("B"),
          atomic("x = 4"),
        EndHandle(),
      EndTry(),
      EndSection()
    )
  )

  testEvents("Test 5",
    s"""  try
       |    If (???) {
       |      x = 1; throw A
       |    } else {
       |      x = 2
       |    }
       |    x = 3
       |  catch A =>
       |    x = 4
       |  ret x""".stripMargin,
    List(
      beginSection("f"),
      BeginTry(),
        atomic("If(???)"),
        Fork(),
          atomic("x = 1"),
          Throw("A"),
        Switch(),
          atomic("x = 2"),
        Join(),
        atomic("x = 3"),
        Fork(),
        Switch(),
          Catching(),
          BeginHandle("A"),
            atomic("x = 4"),
          EndHandle(),
        Join(),
      EndTry(),
      EndSection()
    )
  )

  testEvents("Test 6",
    s"""  try
       |    If (???) {
       |      x = 1; throw A
       |    } else {
       |      x = 2; throw B
       |    }
       |  catch A =>
       |    x = 3
       |  catch B =>
       |    x = 4
       |  ret x""".stripMargin,
    List(
      beginSection("f"),
      BeginTry(),
        atomic("If(???)"),
        Fork(),
          atomic("x = 1"),
          Throw("A"),
        Switch(),
          atomic("x = 2"),
          Throw("B"),
        Join(),
        Catching(),
        Fork(),
          BeginHandle("A"),
            atomic("x = 3"),
          EndHandle(),
        Switch(),
          BeginHandle("B"),
            atomic("x = 4"),
          EndHandle(),
        Join(),
      EndTry(),
      EndSection()
    )
  )

  testEvents("Test 7",
    s"""  try
       |    If (???) {
       |      x = 1; throw A
       |    } else {
       |      x = 2
       |    }
       |    try
       |      x = 3
       |    catch A =>
       |      x = -1
       |  catch A =>
       |    x = 4
       |  ret x""".stripMargin,
    List(
      beginSection("f"),
      BeginTry(),
        atomic("If(???)"),
        Fork(),
          atomic("x = 1"),
          Throw("A"),
        Switch(),
          atomic("x = 2"),
        Join(),
        BeginTry(),
          atomic("x = 3"),
        EndTry(),
        Fork(),
        Switch(),
          Catching(),
          BeginHandle("A"),
            atomic("x = 4"),
          EndHandle(),
        Join(),
      EndTry(),
      EndSection()
    )
  )

}
