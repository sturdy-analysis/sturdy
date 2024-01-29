package sturdy.control

import org.scalatest.funsuite.AnyFunSuite

class TestControlEvents extends AnyFunSuite {
  /**
  * x = a
  * while (x)
  *   x = x - 1
  * y = x
  */
  import ControlEvent.*
  val es = List(
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

  /**
    * def f(n)=
    *   var x;
    *   if (n <= 0)
    *     x = 1
    *   else
    *     x = n * f(n - 1)
    *   ret x
    */
  val es2 = List(
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


  /** def f() =
    *   try
    *     x = 1
    *     throw A
    *     x = 2
    *   catch A =>
    *     x = 3
    *   catch B =>
    *     x = 4
    *   ret x
    */
  val es3 = List(
    Begin("f"),
    BeginTry(),
      Atomic("x = 1"),
      Throw("A"),
      Catch("A"),
      Atomic("x = 3"),
    EndTry(),
    End("f")
  )

  /** def f() =
    *   try
    *     x = 1
    *     throw B
    *     x = 2
    *   catch A =>
    *     x = 3
    *   catch B =>
    *     x = 4
    *   ret x
    */
  val es4 = List(
    Begin("f"),
    BeginTry(),
      Atomic("x = 1"),
      Throw("B"),
      Catch("B"),
      Atomic("x = 4"),
    EndTry(),
    End("f")
  )

  /** def f() =
    *   try
    *     If (???) {
    *       x = 1; throw A
    *     } else {
    *       x = 2
    *     }
    *     x = 3
    *   catch A =>
    *     x = 4
    *   ret x
    */
  val es5 = List(
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

  /** def f() =
    *   try
    *     If (???) {
    *       x = 1; throw A
    *     } else {
    *       x = 2; throw B
    *     }
    *   catch A =>
    *     x = 3
    *   catch B =>
    *     x = 4
    *   ret x
    */
  val es6 = List(
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

  /** def f() =
    *   try
    *     If (???) {
    *       x = 1; throw A
    *     } else {
    *       x = 2
    *     }
    *     try
    *       x = 3
    *     catch A =>
    *       x = -1
    *   catch A =>
    *     x = 4
    *   ret x
    */
  val es7 = List(
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

}
