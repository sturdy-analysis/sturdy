package sturdy.language.tip.abstractions

import sturdy.effect.failure.Failure
import sturdy.values.relational.EqOps
import sturdy.values.Topped
import sturdy.values.integer.{IntInterval, IntSign, given}
import sturdy.language.tip.Interpreter
import sturdy.language.tip.GenericInterpreter.TypeError

object Ints:
  trait Interval extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = IntInterval

    final def topInt(using Instance): IntInterval = IntInterval.Top

    final def asBoolean(v: Value)(using f: Failure): VBool = v match
      case Value.IntValue(i) => EqOps.equ(i, IntInterval(0, 0)).map(!_)
      case Value.TopValue => Topped.Top
      case _ => f.fail(TypeError, s"Expected Int but got $this")

    final def boolean(b: VBool): Value = Value.IntValue(b match
      case Topped.Top => IntInterval(0, 1)
      case Topped.Actual(true) => IntInterval(1, 1)
      case Topped.Actual(false) => IntInterval(0, 0)
    )

  trait Sign extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = IntSign

    final def topInt(using Instance): IntSign = IntSign.TopSign

    final def asBoolean(v: Value)(using f: Failure): VBool = v match
      case Value.IntValue(i) => i match
        case IntSign.Zero => Topped.Actual(false)
        case IntSign.Pos | IntSign.Neg => Topped.Actual(true)
        case _ => Topped.Top
      case Value.TopValue => Topped.Top
      case _ => f.fail(TypeError, s"Expected Int but got $this")

    final def boolean(b: Topped[Boolean]): Value = Value.IntValue(b match
      case Topped.Top => IntSign.ZeroOrPos
      case Topped.Actual(true) => IntSign.Pos
      case Topped.Actual(false) => IntSign.Zero
    )
