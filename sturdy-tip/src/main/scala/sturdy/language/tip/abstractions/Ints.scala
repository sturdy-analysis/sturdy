package sturdy.language.tip.abstractions

import sturdy.effect.failure.Failure
import sturdy.language.tip.TipFailure
import sturdy.values.relational.EqOps
import sturdy.values.Topped
import sturdy.values.integer.{IntSign, NumericInterval, Congruence, given}
import sturdy.language.tip.Interpreter
import sturdy.values.integer.NumericInterval.IsZero

object Ints:
  trait Interval extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = NumericInterval[Int]

    final def topInt: NumericInterval[Int] = NumericInterval.top
    final def topBool: Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using failure: Failure): VBool = v match
      case Value.IntValue(i) => i.isZero.toBoolean
      case Value.TopValue => Topped.Top
      case _ => failure(TipFailure.TypeError, s"Expected Int but got $this")

    final def boolean(b: VBool): Value = Value.IntValue(b match
      case Topped.Top => NumericInterval(0, 1)
      case Topped.Actual(true) => NumericInterval(1, 1)
      case Topped.Actual(false) => NumericInterval(0, 0)
    )

  trait Sign extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = IntSign

    final def topInt: IntSign = IntSign.TopSign
    final def topBool: Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using failure: Failure): VBool = v match
      case Value.IntValue(i) => i match
        case IntSign.Zero => Topped.Actual(false)
        case IntSign.Pos | IntSign.Neg => Topped.Actual(true)
        case _ => Topped.Top
      case Value.TopValue => Topped.Top
      case _ => failure(TipFailure.TypeError, s"Expected Int but got $this")

    final def boolean(b: Topped[Boolean]): Value = Value.IntValue(b match
      case Topped.Top => IntSign.ZeroOrPos
      case Topped.Actual(true) => IntSign.Pos
      case Topped.Actual(false) => IntSign.Zero
    )

  trait CongruenceClass extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = Congruence

    final def topInt: Congruence = Congruence.top
    final def topBool: Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using failure: Failure): VBool = v match
      case Value.IntValue(i) => i match
        case Congruence(0,0) => Topped.Actual(false)
        case Congruence(0,_) => Topped.Top
        case _ => Topped.Actual(true)
      case Value.TopValue => Topped.Top
      case _ => failure(TipFailure.TypeError, s"Expected Int but got $this")

    final def boolean(b: Topped[Boolean]): Value = Value.IntValue(b match
      case Topped.Top => Congruence(0,1)
      case Topped.Actual(true) => Congruence(1,0)
      case Topped.Actual(false) => Congruence(0,0)
    )
