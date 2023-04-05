package sturdy.language.tip.abstractions

import apron.{Linexpr1, Texpr1CstNode}
import sturdy.effect.failure.Failure
import sturdy.language.tip.TipFailure
import sturdy.values.relational.EqOps
import sturdy.values.{Join, Topped}
import sturdy.values.integer.{AbstractBitVector, ApronValue, IntSign, NumericInterval, given}
import sturdy.language.tip.Interpreter

object Ints:
  trait Interval extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = NumericInterval[Int]

    final def topInt: NumericInterval[Int] = NumericInterval(Int.MinValue, Int.MaxValue)
    final def topBool: Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using failure: Failure): VBool = v match
      case Value.IntValue(i) => i.toBoolean
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

  trait BitVectors extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = AbstractBitVector[Int]

    final def topInt: AbstractBitVector[Int] = TopAbstractBitVectorInt.top

    final def topBool: Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using failure: Failure): VBool = v match
      case Value.IntValue(i) => i.toBoolean
      case Value.TopValue => Topped.Top
      case _ => failure(TipFailure.TypeError, s"Expected Int but got $this")

    final def boolean(b: VBool): Value = Value.IntValue(b match
      case Topped.Top => Join(AbstractBitVector.constant(0), AbstractBitVector.constant(1)).get
      case Topped.Actual(true) => AbstractBitVector.constant(1)
      case Topped.Actual(false) => AbstractBitVector.constant(0)
    )
