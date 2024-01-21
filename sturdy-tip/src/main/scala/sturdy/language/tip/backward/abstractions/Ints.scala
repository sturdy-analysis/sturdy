package sturdy.language.tip.backward.abstractions

import sturdy.effect.failure.Failure
import sturdy.language.tip.backward.BackwardsInterpreter
import sturdy.language.tip.{Interpreter, TipFailure}
import sturdy.values.integer.{AbstractBitVector, IntSign, Interval, given}
import sturdy.values.relational.EqOps
import sturdy.values.{Join, Topped}

object Ints:
  trait MInterval extends BackwardsInterpreter :
    final type VBool = Topped[Boolean]
    final type VInt = Interval

    final def topInt: Interval = Interval.I(Int.MinValue, Int.MaxValue)
    final def topBool: Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using failure: Failure): VBool = v match
      case Value.IntValue(i) => i match
        case Interval.I(l,h) => if l == 0 && h == 0 then Topped.Actual(false) else Topped.Actual(true)
        case Interval.ITop   => Topped.Top
      case Value.TopValue => Topped.Top
      case _ => failure(TipFailure.TypeError, s"Expected Int but got $this")

    final def boolean(b: VBool): Value = Value.IntValue(b match
      case Topped.Top => Interval.ITop
      case Topped.Actual(true) => Interval.I(1.0, 2.0)
      case Topped.Actual(false) => Interval.I(0.0, 0.0)
    )

  trait Sign extends BackwardsInterpreter :
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

  trait BitVectors extends BackwardsInterpreter :
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