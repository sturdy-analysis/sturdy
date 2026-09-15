package sturdy.language.wasm.analyses

import sturdy.{*, given}
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.generic.{FunctionInstance, given}
import ConstantAnalysis.*
import sturdy.values.given
import sturdy.values.Abstractly
import sturdy.values.PartialOrder
import sturdy.values.Topped
import sturdy.values.toppedPartialOrder
import sturdy.values.concretePO
import sturdy.values.integer.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.effect.symboltable.IntervalMappedSymbolTable.{*, given}

import java.nio.ByteBuffer

object ConstantAnalysisSoundness {

  given poFloat: PartialOrder[Float] with
    override def lteq(f1: Float, f2: Float): Boolean =
      f1.isNaN && f2.isNaN || f1 == f2
      
  given poDouble: PartialOrder[Double] with
    override def lteq(d1: Double, d2: Double): Boolean =
      d1.isNaN && d2.isNaN || d1 == d2

  given po: PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x, y) match
      case (_, Value.TopValue) => true
      case (Value.Num(NumValue.Int32(i1)), Value.Num(NumValue.Int32(i2))) => PartialOrder[I32].lteq(i1, i2)
      case (Value.Num(NumValue.Int64(l1)), Value.Num(NumValue.Int64(l2))) => PartialOrder[I64].lteq(l1, l2)
      case (Value.Num(NumValue.Float32(f1)), Value.Num(NumValue.Float32(f2))) => PartialOrder[F32].lteq(f1, f2)
      case (Value.Num(NumValue.Float64(d1)), Value.Num(NumValue.Float64(d2))) => PartialOrder[F64].lteq(d1, d2)
      case (Value.Ref(r1), Value.Ref(r2)) =>
        r1.set.forall { f1 =>
          r2.set.exists { f2 =>
            (f1, f2) match {
              case (inst1: FunctionInstance, inst2: FunctionInstance) => PartialOrder[Topped[FunctionInstance]].lteq(Topped.Actual(inst1), Topped.Actual(inst2))
              case (ext1: ExternReference, ext2: ExternReference) => ext1 == ext2
              case _ => false
            }
          }
        }
      case (Value.Vec(v1), Value.Vec(v2)) => lteqVecs(v1, v2)
      case _ => false
    
  given [C,A](using aValue: Abstractly[C,A]): Abstractly[List[C], List[A]] with
    override def apply(c: List[C]): List[A] =
      c.map(aValue.apply)

  given [A](using poValue: PartialOrder[A]): PartialOrder[List[A]] with
    override def lteq(x: List[A], y: List[A]): Boolean =
      if (x.length != y.length)
        false
      else
        x.zip(y).forall((a,b) => poValue.lteq(a,b))

  given (using bs: Soundness[Byte,Byte]): Soundness[Byte, Topped[Byte]] with
    def isSound(cByte: Byte, aByte: Topped[Byte]): IsSound = aByte match
      case Topped.Top => IsSound.Sound
      case Topped.Actual(b) => bs.isSound(cByte,b)
      
  given Soundness[ConcreteInterpreter.FunV, FunV] with
    override def isSound(cFun: ConcreteInterpreter.FunV, aFun: FunV): IsSound =
      powersetContainsOneSound.isSound(cFun, aFun)

  given Soundness[ConcreteInterpreter.Instance, ConstantAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: ConstantAnalysis.Instance): IsSound =
      // soundness for stack, memory, symbol table, call frame
      a.stack.operandStackIsSound(c.stack) &&
        a.memory.memoryIsSound(c.memory) &&
        a.globals.tableIsSound(c.globals) &&
        a.tables.tableIsSound(c.tables) &&
        a.callFrame.isSound(c.callFrame)
}

  def lteqVecs(b1: Topped[Array[Byte]], b2: Topped[Array[Byte]]): Boolean =
    if (b1.isTop) return false
    if (b2.isTop) return true

    val bb1 = ByteBuffer.wrap(b1.get)
    val bb2 = ByteBuffer.wrap(b2.get)

    val eqF32 = (0 until 16 by 4).forall { i =>
      val x = java.lang.Float.intBitsToFloat(bb1.getInt(i))
      val y = java.lang.Float.intBitsToFloat(bb2.getInt(i))
      if (x.isNaN && y.isNaN) true else bb1.getInt(i) == bb2.getInt(i)
    }

    val eqF64 = (0 until 16 by 8).forall { i =>
      val x = java.lang.Double.longBitsToDouble(bb1.getLong(i))
      val y = java.lang.Double.longBitsToDouble(bb2.getLong(i))
      if (x.isNaN && y.isNaN) true else bb1.getLong(i) == bb2.getLong(i)
    }

    eqF32 || eqF64