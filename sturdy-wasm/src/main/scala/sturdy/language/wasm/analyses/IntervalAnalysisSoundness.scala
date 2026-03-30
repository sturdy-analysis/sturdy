package sturdy.language.wasm.analyses

import sturdy.{*, given}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.generic.{FunctionInstance, given}
import IntervalAnalysis.*
import sturdy.language.wasm.ConcreteInterpreter.ExternReference
import sturdy.values.given
import sturdy.values.Abstractly
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.toppedPartialOrder
import sturdy.values.concretePO
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.Powerset

object IntervalAnalysisSoundness {

  given poFloat: PartialOrder[Float] with
    override def lteq(f1: Float, f2: Float): Boolean =
      f1.isNaN && f2.isNaN || f1 == f2
      
  given poDouble: PartialOrder[Double] with
    override def lteq(d1: Double, d2: Double): Boolean =
      d1.isNaN && d2.isNaN || d1 == d2
  
  given po: PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x,y) match
      case (_, Value.TopValue) => true
      case (Value.Num(NumValue.Int32(i1)), Value.Num(NumValue.Int32(i2))) => PartialOrder[NumericInterval[Int]].lteq(i1,i2)
      case (Value.Num(NumValue.Int64(l1)), Value.Num(NumValue.Int64(l2))) => PartialOrder[NumericInterval[Long]].lteq(l1,l2)
      case (Value.Num(NumValue.Float32(f1)), Value.Num(NumValue.Float32(f2))) => PartialOrder[Topped[Float]].lteq(f1,f2)
      case (Value.Num(NumValue.Float64(d1)), Value.Num(NumValue.Float64(d2))) => PartialOrder[Topped[Double]].lteq(d1,d2)
      case (Value.ExnRef(es1), Value.ExnRef(es2)) =>
        es1.forall { case (t1, fs1) =>
          es2.exists { case (t2, fs2) =>
            (t1 eq t2) && fs1.size == fs2.size && fs1.zip(fs2).forall((f1, f2) => lteq(f1, f2))
          }
        }
      case (Value.Ref(r1), Value.Ref(r2)) =>
        val p1 = r1.asInstanceOf[Powerset[FunctionInstance | ExternReference]]
        val p2 = r2.asInstanceOf[Powerset[FunctionInstance | ExternReference]]
        p1.set.forall { f1 =>
          p2.set.exists { f2 =>
            (f1, f2) match {
              case (inst1: FunctionInstance, inst2: FunctionInstance) => PartialOrder[Topped[FunctionInstance]].lteq(Topped.Actual(inst1), Topped.Actual(inst2))
              case (ext1: ExternReference, ext2: ExternReference) => ext1 == ext2
              case _ => false
            }
          }
        }
      case _ => false
    
  given [C,A](using aValue: Abstractly[C,A]): Abstractly[List[C], List[A]] with
    override def apply(c: List[C]): List[A] =
      c.map(aValue.apply)

  given (using bs: Soundness[Byte,Byte]): Soundness[Byte, Topped[Byte]] with
    def isSound(cByte: Byte, aByte: Topped[Byte]): IsSound = aByte match
      case Topped.Top => IsSound.Sound
      case Topped.Actual(b) => bs.isSound(cByte,b)
      
  given Soundness[ConcreteInterpreter.FunV, FunV] with
    override def isSound(cFun: ConcreteInterpreter.FunV, aFun: FunV): IsSound =
      powersetContainsOneSound.isSound(cFun, aFun)

  given Soundness[ConcreteInterpreter.Instance, IntervalAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: IntervalAnalysis.Instance): IsSound =
      // soundness for stack, memory, symbol tables, call frame
      a.stack.operandStackIsSound(c.stack) &&
        a.memory.memoryIsSound(c.memory) &&
        a.globals.tableIsSound(c.globals) &&
//        a.tables.tableIsSound(c.tables) &&
        a.callFrame.isSound(c.callFrame)
}
