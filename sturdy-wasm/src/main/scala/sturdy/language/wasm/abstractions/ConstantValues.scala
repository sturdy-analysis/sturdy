package sturdy.language.wasm.abstractions

import sturdy.data.{*, given}
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.OperandStack
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.{FixIn, FixOut, FunctionInstance, InstLoc}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.{Finite, Join, Powerset, Topped, given}
import sturdy.values.booleans.given
import sturdy.values.floating.given
import sturdy.values.integer.given
import swam.OpCode
import swam.syntax
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import scala.collection.MapView

trait ConstantValues extends Interpreter with PowersetReference:
  final type I32 = Topped[Int]
  final type I64 = Topped[Long]
  final type F32 = Topped[Float]
  final type F64 = Topped[Double]
  final type V128 = Topped[Array[Byte]]
  final type Bool = Topped[Boolean]

  final def topI32: I32 = Topped.Top
  final def topI64: I64 = Topped.Top
  final def topF32: F32 = Topped.Top
  final def topF64: F64 = Topped.Top
  final def topV128: V128 = Topped.Top

  final def asBoolean(v: Value)(using Failure): Bool = v.asInt32 match
    case Topped.Top => Topped.Top
    case Topped.Actual(i) => Topped.Actual(i != 0)
  final def booleanToVal(b: Bool): Value = b match
    case Topped.Top => Value.Num(NumValue.Int32(topI32))
    case Topped.Actual(true) => Value.Num(NumValue.Int32(Topped.Actual(1)))
    case Topped.Actual(false) => Value.Num(NumValue.Int32(Topped.Actual(0)))

  def constantInstructions(analysis: Instance): ConstantInstructionsLogger =
    val constants = new ConstantInstructionsLogger(analysis.stack)(using analysis.failure)
    analysis.fixpoint.addContextFreeLogger(constants)
    constants

  class ConstantInstructionsLogger(stack: OperandStack[Value, MayJoin.NoJoin])(using Failure) extends InstructionResultLogger[Value,Value](stack):
    override def boolValue(v: Value): Value = booleanToVal(asBoolean(v))
    override def dummyValue: Value = Value.Num(NumValue.Int32(Topped.Actual(0)))
    override def getInfo(v: Value): Value = v

    def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
      case Value.TopValue => false
      case Value.Num(NumValue.Int32(v)) => v.isActual
      case Value.Num(NumValue.Int64(v)) => v.isActual
      case Value.Num(NumValue.Float32(v)) => v.isActual
      case Value.Num(NumValue.Float64(v)) => v.isActual
      case Value.Ref(RefValue.RefValue(v)) => v.size == 1
      case Value.Vec(VecValue.Vec128(v)) => v.isActual
    })

    def grouped: Map[String, Map[InstLoc, List[Value]]] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

    def groupedCount: Map[String, Int] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap
