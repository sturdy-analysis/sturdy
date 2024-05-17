package sturdy.language.tip.abstractions

import sturdy.effect.failure.Failure
import sturdy.language.tip.TipFailure
import sturdy.values.ordering.EqOps
import sturdy.values.{Topped, Join}
import sturdy.values.integer.{IntSign, NumericInterval, AbstractBitVector, given}
import sturdy.language.tip.Interpreter

object Ints:
  trait Interval extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = NumericInterval[Int]

    final def topInt(using Instance): NumericInterval[Int] = NumericInterval(Int.MinValue, Int.MaxValue)
    final def topBool(using Instance): Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using inst: Instance): VBool = v match
      case Value.BoolValue(b) => b
      case Value.IntValue(i) => i.toBoolean
      case Value.TopValue => Topped.Top
      case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

    final def asInt(v: Value)(using inst: Instance): VInt = v match
      case Value.BoolValue(b) => b match
        case Topped.Top => NumericInterval(0, 1)
        case Topped.Actual(true) => NumericInterval(1, 1)
        case Topped.Actual(false) => NumericInterval(0, 0)
      case Value.IntValue(i) => i
      case Value.TopValue => NumericInterval(Int.MinValue, Int.MaxValue)
      case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  trait Sign extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = IntSign

    final def topInt(using Instance): IntSign = IntSign.TopSign
    final def topBool(using Instance): Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using inst: Instance): VBool = v match
      case Value.BoolValue(b) => b
      case Value.IntValue(i) => i match
        case IntSign.Zero => Topped.Actual(false)
        case IntSign.Pos | IntSign.Neg => Topped.Actual(true)
        case _ => Topped.Top
      case Value.TopValue => Topped.Top
      case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

    final def boolean(b: Topped[Boolean]): Value = Value.IntValue(b match
      case Topped.Top => IntSign.ZeroOrPos
      case Topped.Actual(true) => IntSign.Pos
      case Topped.Actual(false) => IntSign.Zero
    )

    final def asInt(v: Value)(using inst: Instance): VInt = v match
      case Value.BoolValue(b) => b match
        case Topped.Top => IntSign.ZeroOrPos
        case Topped.Actual(true) => IntSign.Pos
        case Topped.Actual(false) => IntSign.Zero
      case Value.IntValue(i) => i
      case Value.TopValue => IntSign.TopSign
      case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  trait BitVectors extends Interpreter :
    final type VBool = Topped[Boolean]
    final type VInt = AbstractBitVector[Int]

    final def topInt: AbstractBitVector[Int] = TopAbstractBitVectorInt.top

    final def topBool(using Instance): Topped[Boolean] = Topped.Top

    final def asBoolean(v: Value)(using failure: Failure): VBool = v match
      case Value.IntValue(i) => i.toBoolean
      case Value.TopValue => Topped.Top
      case _ => failure(TipFailure.TypeError, s"Expected Int but got $this")

    final def boolean(b: VBool): Value = Value.IntValue(b match
      case Topped.Top => Join(AbstractBitVector.constant(0), AbstractBitVector.constant(1)).get
      case Topped.Actual(true) => AbstractBitVector.constant(1)
      case Topped.Actual(false) => AbstractBitVector.constant(0)
    )

