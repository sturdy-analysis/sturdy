package sturdy.language.wasm.abstractions

import sturdy.data.CombineEquiList
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.FixIn
import sturdy.language.wasm.generic.FixOut
import sturdy.language.wasm.generic.InstLoc
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.{CombineToppedFlat, Finite, Join, Topped, given}
import sturdy.values.booleans.given
import sturdy.values.floating.given
import sturdy.values.integer.given
import swam.OpCode
import swam.syntax
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import scala.collection.MapView

trait ConstantValues extends Interpreter:
  final type I32 = Topped[Int]
  final type I64 = Topped[Long]
  final type F32 = Topped[Float]
  final type F64 = Topped[Double]
  final type Bool = Topped[Boolean]

  given Join[I32] = new CombineToppedFlat
  given Join[I64] = new CombineToppedFlat
  given Join[F32] = new CombineToppedFlat
  given Join[F64] = new CombineToppedFlat
  given Join[Bool] = new CombineToppedFlat

  final def topI32: I32 = Topped.Top
  final def topI64: I64 = Topped.Top
  final def topF32: F32 = Topped.Top
  final def topF64: F64 = Topped.Top

  final def asBoolean(v: Value)(using Failure): Bool = v.asInt32 match
    case Topped.Top => Topped.Top
    case Topped.Actual(i) => Topped.Actual(i != 0)
  final def boolean(b: Bool): Value = b match
    case Topped.Top => Value.Int32(topI32)
    case Topped.Actual(true) => Value.Int32(Topped.Actual(1))
    case Topped.Actual(false) => Value.Int32(Topped.Actual(0))

  def constantInstructions(analysis: Instance): ConstantInstructionsLogger =
    val constants = new ConstantInstructionsLogger(analysis.stack)(using analysis.failure)
    analysis.fixpoint.addContextFreeLogger(constants)
    constants

  class ConstantInstructionsLogger(stack: DecidableOperandStack[Value])(using Failure) extends InstructionResultLogger[Value](stack):
    override def boolValue(v: Value): Value = boolean(asBoolean(v))
    override def dummyValue: Value = Value.Int32(Topped.Actual(0))

    def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
      case Value.TopValue => false
      case Value.Int32(v) => v.isActual
      case Value.Int64(v) => v.isActual
      case Value.Float32(v) => v.isActual
      case Value.Float64(v) => v.isActual
    })

    def grouped: Map[String, Map[InstLoc, List[Value]]] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

    def groupedCount: Map[String, Int] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap
