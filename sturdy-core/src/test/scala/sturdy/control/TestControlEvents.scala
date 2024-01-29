package sturdy.control

import org.scalatest.funsuite.AnyFunSuite
import ControlEvent.*

class TestControlEvents extends AnyFunSuite {
  inline def testEvents[A,S,E](name: String, code: String, es: List[ControlEvent[A,S,E]]): Unit =
    test(name) {
      val rec = new RecordingControlObserver[A, S, E](true)
      es.foreach(rec.handle)
      rec.ch
    }

  testEvents("Test 1",
    s"""
     |x = a
     |while (x)
     |  x = x - 1
     |y = x
     |""".stripMargin,
    List(
      Start(),
      Atomic("x = a"),
      Atomic("while (x)"),
      Atomic("x = x - 1"),
      Atomic("while (x)"),
      FixpointAbort(),
      FixpointRepeat(),
      Atomic("while (x)"),
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
      Start(),
      Begin("f"),
      Atomic("if"),
      Fork(),
      Atomic("x = 1"),
      Switch(),
      FixpointAbort(),
      Join(),
      Atomic("ret x"),
      End("f"),

      FixpointRepeat(),
      Begin("f"),
      Atomic("if"),
      Fork(),
      Atomic("x = 1"),
      Switch(),
      Atomic("x = n * f(n-1)"),
      Join(),
      Atomic("ret x"),
      End("f"),
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
      Start(),
      Begin("f"),
      BeginTry(),
        Atomic("x = 1"),
        Throw("A"),
        Catch("A"),
        Atomic("x = 3"),
      EndTry(),
      End("f")
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
      Start(),
      Begin("f"),
      BeginTry(),
        Atomic("x = 1"),
        Throw("B"),
        Catch("B"),
        Atomic("x = 4"),
      EndTry(),
      End("f")
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
      Start(),
      Begin("f"),
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
          Catch("A"),
          Atomic("x = 4"),
        Join(),
      EndTry(),
      End("f")
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
      Start(),
      Begin("f"),
      BeginTry(),
        Atomic("If(???)"),
        Fork(),
          Atomic("x = 1"),
          Throw("A"),
        Switch(),
          Atomic("x = 2"),
          Throw("B"),
        Join(),
        Fork(),
          Catch("A"),
          Atomic("x = 3"),
        Switch(),
          Catch("B"),
          Atomic("x = 4"),
        Join(),
      EndTry(),
      End("f")
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
      Start(),
      Begin("f"),
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
          Catch("A"),
          Atomic("x = 4"),
        Join(),
      EndTry(),
      End("f")
    )
  )

}
