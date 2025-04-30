package sturdy.language.wasm.abstractions

import sturdy.data.CombineEquiList
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.{FixIn, FixOut, InstLoc}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.booleans.given
import sturdy.values.floating.given
import sturdy.values.{Finite, Join, Topped, given}
import sturdy.values.integer.{NumericInterval, given}
import swam.{OpCode, syntax}
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import scala.collection.MapView

trait IntervalValues extends Interpreter:
  final type I32 = NumericInterval[Int]
  final type I64 = NumericInterval[Long]
  final type F32 = Topped[Float]
  final type F64 = Topped[Double]
  final type Bool = Topped[Boolean]
  final type FuncReference = Topped[Int]
  final type ExternReference = Topped[Int]

  final def topI32: I32 = NumericInterval(Integer.MIN_VALUE, Integer.MAX_VALUE)
  final def topI64: I64 = NumericInterval(Long.MinValue, Long.MaxValue)
  final def topF32: F32 = Topped.Top
  final def topF64: F64 = Topped.Top
  final def topFuncRef: FuncReference = Topped.Top
  final def topExternRef: ExternReference = Topped.Top

  final def asBoolean(v: Value)(using Failure): Bool =
    v.asInt32.toBoolean

  final def boolean(b: Bool): Value = b match
    case Topped.Top => Value.Num(NumValue.Int32(NumericInterval(0, 1)))
    case Topped.Actual(true) => Value.Num(NumValue.Int32(NumericInterval(1, 1)))
    case Topped.Actual(false) => Value.Num(NumValue.Int32(NumericInterval(0, 0)))

  def constantInstructions(analysis: Instance): ConstantInstructionsLogger =
    val constants = new ConstantInstructionsLogger(analysis.stack)(using analysis.failure)
    analysis.fixpoint.addContextFreeLogger(constants)
    constants

  class ConstantInstructionsLogger(stack: DecidableOperandStack[Value])(using Failure) extends InstructionResultLogger[Value](stack):
    override def boolValue(v: Value): Value = boolean(asBoolean(v))
    override def dummyValue: Value = Value.Num(NumValue.Int32(NumericInterval(0, 0)))

    def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
      case Value.TopValue => false
      case Value.Num(NumValue.Int32(v)) => v.isConstant
      case Value.Num(NumValue.Int32(v)) => v.isConstant
      //case Value.Num(NumValue.Int32(v)) => v.isActual
      //case Value.Num(NumValue.Int32(v)) => v.isActual
    })

    def grouped: Map[String, Map[InstLoc, List[Value]]] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

    def groupedCount: Map[String, Int] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap
