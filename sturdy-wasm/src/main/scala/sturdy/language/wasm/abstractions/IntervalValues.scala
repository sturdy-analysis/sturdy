package sturdy.language.wasm.abstractions

import sturdy.data.{*, given}
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.OperandStack
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.{FixIn, FixOut, FuncId, FunctionInstance, InstLoc}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.booleans.given
import sturdy.values.floating.given
import sturdy.values.{Finite, Join, Powerset, Topped, given}
import sturdy.values.integer.{IntegerOps, NumericInterval, given}
import sturdy.values.references.AbstractReference
import swam.{OpCode, syntax}
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import scala.collection.MapView

trait IntervalValues extends Interpreter with PowersetReference:

  enum I32Val:
    case Num(numericInterval: NumericInterval[Int])
    case GlobalAddr(nameAndBaseAddr: Powerset[(String,Int)], offset: NumericInterval[Int])
    case StackAddr(function: Powerset[FuncId], frameSize: NumericInterval[Int], stackPointer: NumericInterval[Int], baseOffset: Powerset[Int], otherOffset: NumericInterval[Int])
    case HeapAddr(sites: AbstractReference[Unit], size: NumericInterval[Int], baseOffset: Powerset[Int], otherOffset: NumericInterval[Int])

    def asNum(using ivIntOps: IntegerOps[Int, NumericInterval[Int]]): NumericInterval[Int] =
      this match
        case Num(iv) => iv
        case GlobalAddr(nameAndBase, offset) =>
          val base = nameAndBase.set.iterator.map((_, start) => NumericInterval(start, start)).reduce(Join(_,_).get)
          ivIntOps.add(base, offset)
        case StackAddr(_, _, stackPointer, baseOffsets, otherOffset) =>
          val baseOffset = baseOffsets.set.iterator.map(offset => NumericInterval(offset, offset)).reduce(Join(_,_).get)
          ivIntOps.add(ivIntOps.add(stackPointer, baseOffset), otherOffset)
        case HeapAddr(_, _, _, _) =>
          NumericInterval(Integer.MIN_VALUE, Integer.MAX_VALUE)

  import I32Val.*

  final type I32 = I32Val
  final type I64 = NumericInterval[Long]
  final type F32 = Topped[Float]
  final type F64 = Topped[Double]
  final type V128 = Topped[Array[Byte]]
  final type Bool = Topped[Boolean]

  final def topI32: I32 = Num(NumericInterval(Integer.MIN_VALUE, Integer.MAX_VALUE))
  final def topI64: I64 = NumericInterval(Long.MinValue, Long.MaxValue)
  final def topF32: F32 = Topped.Top
  final def topF64: F64 = Topped.Top
  final def topV128: V128 = Topped.Top

  final def asBoolean(v: Value)(using Failure): Bool =
    v.asInt32.asNum.toBoolean

  final def booleanToVal(b: Bool): Value = b match
    case Topped.Top => Value.Num(NumValue.Int32(Num(NumericInterval(0, 1))))
    case Topped.Actual(true) => Value.Num(NumValue.Int32(Num(NumericInterval(1, 1))))
    case Topped.Actual(false) => Value.Num(NumValue.Int32(Num(NumericInterval(0, 0))))

  def constantInstructions(analysis: Instance): ConstantInstructionsLogger =
    val constants = new ConstantInstructionsLogger(analysis.stack)(using analysis.failure)
    analysis.fixpoint.addContextFreeLogger(constants)
    constants

  class ConstantInstructionsLogger(stack: OperandStack[Value, MayJoin.NoJoin])(using Failure) extends InstructionResultLogger[Value,Value](stack):
    override def boolValue(v: Value): Value = booleanToVal(asBoolean(v))
    override def getInfo(v: Value): Value = v

    def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
      case Value.TopValue => false
      case Value.Num(NumValue.Int32(v)) => v.asNum.isConstant
      case Value.Num(NumValue.Int64(v)) => v.isConstant
      case Value.Num(NumValue.Float32(v)) => v.isActual
      case Value.Num(NumValue.Float64(v)) => v.isActual
      case Value.Ref(v) => v.size == 1
      case Value.Vec(v) => v.isActual
    })

    def grouped: Map[String, Map[InstLoc, List[Value]]] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

    def groupedCount: Map[String, Int] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap
