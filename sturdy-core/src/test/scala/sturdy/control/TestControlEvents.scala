package sturdy.control

import org.scalatest.funsuite.AnyFunSuite
import BasicControlEvent.*, ExceptionControlEvent.*, BranchingControlEvent.*, FixpointControlEvent.*

class TestControlEvents extends AnyFunSuite {
  inline def testEvents[A,S,E,F](name: String, code: String, es: List[ControlEvent]): Unit =
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
      Atomic("x = a"),
      Atomic("while (x)"),
      BeginFixpoint("while"),
      Atomic("x = x - 1"),
      Atomic("while (x)"),
      Recurrent(true),
      EndFixpoint(),
      RepeatFixpoint(),
      Atomic("x = x - 1"),
      Atomic("while (x)"),
      Atomic("y = x")
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
      BeginSection("f"),
      BeginFixpoint("fun f"),
      Atomic("if"),
      Fork(),
      Atomic("x = 1"),
      Switch(),
      BeginSection("f"),
      Recurrent(true),
      EndSection(),
      Join(),
      Atomic("ret x"),
      EndFixpoint(),

      RepeatFixpoint(),
      Atomic("if"),
      Fork(),
      Atomic("x = 1"),
      Switch(),
      Atomic("x = n * f(n-1)"),
      Join(),
      Atomic("ret x"),
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
      BeginSection("f"),
      BeginTry(),
        Atomic("x = 1"),
        Throw("A"),
        Catching(),
        Handle("A"),
        Atomic("x = 3"),
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
      BeginSection("f"),
      BeginTry(),
        Atomic("x = 1"),
        Throw("B"),
        Catching(),
        Handle("B"),
        Atomic("x = 4"),
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
      BeginSection("f"),
      BeginTry(),
        Atomic("If(???)"),
        Fork(),
          Atomic("x = 1"),
          Throw("A"),
        Switch(),
          Atomic("x = 2"),
        Join(),
        Atomic("x = 3"),
        Fork(),
        Switch(),
          Catching(),
          Handle("A"),
          Atomic("x = 4"),
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
      BeginSection("f"),
      BeginTry(),
        Atomic("If(???)"),
        Fork(),
          Atomic("x = 1"),
          Throw("A"),
        Switch(),
          Atomic("x = 2"),
          Throw("B"),
        Join(),
        Catching(),
        Fork(),
          Handle("A"),
          Atomic("x = 3"),
        Switch(),
          Handle("B"),
          Atomic("x = 4"),
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
      BeginSection("f"),
      BeginTry(),
        Atomic("If(???)"),
        Fork(),
          Atomic("x = 1"),
          Throw("A"),
        Switch(),
          Atomic("x = 2"),
        Join(),
        BeginTry(),
          Atomic("x = 3"),
        EndTry(),
        Fork(),
        Switch(),
          Catching(),
          Handle("A"),
          Atomic("x = 4"),
        Join(),
      EndTry(),
      EndSection()
    )
  )

}
