package sturdy.language.tip.abstractions

import sturdy.effect.failure.Failure
import sturdy.ir.IR
import sturdy.language.tip.{Interpreter, TipFailure, TypeAnno}
import sturdy.values.ordering.EqOps
import sturdy.values.{Join, Topped}
import sturdy.values.integer.{AbstractBitVector, IntSign, NumericInterval, given}
import sturdy.values.types.BaseType

object Ints:
  trait Interval extends Interpreter :
    final type VInt = NumericInterval[Int]
    override def topBool: VInt = NumericInterval(0, 1)
    final def topInt(using Instance): NumericInterval[Int] = NumericInterval(Int.MinValue, Int.MaxValue)

  trait Sign extends Interpreter :
    final type VInt = IntSign
    override def topBool: VInt = IntSign.ZeroOrPos
    final def topInt(using Instance): IntSign = IntSign.TopSign

  trait BitVectors extends Interpreter :
    final type VInt = AbstractBitVector[Int]
    override def topBool: VInt = Join(AbstractBitVector.constant(0), AbstractBitVector.constant(1)).get
    final def topInt: AbstractBitVector[Int] = TopAbstractBitVectorInt.top

  trait IRInts extends Interpreter:
    final type VInt = IR
    override def topBool: VInt = IR.Unknown()
    final def topInt(using Instance): IR = IR.Unknown()

  trait Types extends Interpreter:
    final type VInt = BaseType[Int]
    override def topBool: VInt = BaseType[Int]
    final def topInt(using Instance): VInt = BaseType[Int]

