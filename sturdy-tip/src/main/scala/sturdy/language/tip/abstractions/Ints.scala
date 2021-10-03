package sturdy.language.tip.abstractions

import sturdy.values.relational.EqOps
import sturdy.values.Topped
import sturdy.values.ints.{IntInterval, IntIntervalApron, IntSign, given}
import sturdy.language.tip.Interpreter

object Ints:
  trait Interval extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = IntIntervalApron

    final def topInt(using Interpreter): IntIntervalApron = IntIntervalApron.Top

    final def asBoolean(v: Value): VBool = v match
      case Value.IntValue(i) => EqOps.equ(i, IntIntervalApron(0, 0)).map(!_)
      case Value.TopValue => Topped.Top
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")

    final def boolean(b: VBool): Value = Value.IntValue(b match
      case Topped.Top => IntIntervalApron(0, 1)
      case Topped.Actual(true) => IntIntervalApron(1, 1)
      case Topped.Actual(false) => IntIntervalApron(0, 0)
    )

  trait Sign extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = IntSign

    final def topInt(using Interpreter): IntSign = IntSign.TopSign

    final def asBoolean(v: Value): VBool = v match
      case Value.IntValue(i) => i match
        case IntSign.Zero => Topped.Actual(false)
        case IntSign.Pos | IntSign.Neg => Topped.Actual(true)
        case _ => Topped.Top
      case Value.TopValue => Topped.Top
      case _ => throw new IllegalArgumentException(s"Expected Int but got $this")

    final def boolean(b: Topped[Boolean]): Value = Value.IntValue(b match
      case Topped.Top => IntSign.ZeroOrPos
      case Topped.Actual(true) => IntSign.Pos
      case Topped.Actual(false) => IntSign.Zero
    )
