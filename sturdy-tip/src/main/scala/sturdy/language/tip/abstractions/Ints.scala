package sturdy.language.tip.abstractions

import sturdy.effect.failure.Failure
import sturdy.values.abstraction.symbolic.*
import sturdy.values.relational.EqOps
import sturdy.values.Topped
import sturdy.values.integer.{IntSign, NumericInterval, given}
import sturdy.language.tip.Interpreter
import sturdy.language.tip.GenericInterpreter.TypeError
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
      case _ => failure(TypeError, s"Expected Int but got $this")

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
      case _ => failure(TypeError, s"Expected Int but got $this")

    final def boolean(b: Topped[Boolean]): Value = Value.IntValue(b match
      case Topped.Top => IntSign.ZeroOrPos
      case Topped.Actual(true) => IntSign.Pos
      case Topped.Actual(false) => IntSign.Zero
    )

  trait Relational extends Interpreter :
    given symbolic: SymbolicValue[IntExp[String], String, NumericInterval[Int]] = new SymbolicValue {
      override def embedValue(v: NumericInterval[Int]): String = ???
      override def extractValue(s: String): NumericInterval[Int] = ???
      override def embedTree(t: IntExp[String]): String = ???
      override def assign(s: String, t: IntExp[String]): Unit = ???
    }

    final type VBool = Topped[Boolean]
    final type VInt = IntExp[String]

    final def topInt: VInt = IntExp.Symbol(symbolic.embedValue(NumericInterval.top))
    final def topBool: Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using failure: Failure): VBool = v match
      case Value.IntValue(i) => symbolic.embedTreeAndExtractValue(i).isZero.toBoolean
      case Value.TopValue => Topped.Top
      case _ => failure(TypeError, s"Expected Int but got $this")

    final def boolean(b: VBool): Value = Value.IntValue(b match
      case Topped.Top => IntExp.Symbol(symbolic.embedValue(NumericInterval(0, 1)))
      case Topped.Actual(true) => IntExp.IntLit(1)
      case Topped.Actual(false) => IntExp.IntLit(0)
    )
