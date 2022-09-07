package sturdy.language.tip.abstractions

import sturdy.effect.failure.Failure
import sturdy.language.tip.{Interpreter, TipFailure}
import sturdy.values.Topped
import sturdy.values.integer.NumericInterval.IsZero
import sturdy.values.integer.{IntSign, NumericInterval, given}
import sturdy.values.relational.EqOps

object Strings:
  trait CharacterInclusion extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = NumericInterval[Int]
    final type VString = Topped[String]

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
