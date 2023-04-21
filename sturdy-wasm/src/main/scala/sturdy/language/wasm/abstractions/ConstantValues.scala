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
import sturdy.values.{CombineToppedDeep, CombineToppedFlat, Finite, Join, Powerset, Reduce, ReducedProduct, Topped, Widening, given}
import sturdy.values.booleans.given
import sturdy.values.floating.given
import sturdy.values.integer.{IntegerOps, given}
import swam.OpCode
import swam.syntax
import swam.syntax.{Func, LoadInst, LoadNInst, StoreInst, StoreNInst}

import scala.collection.MapView

trait ConstantValues(getModule: () => swam.syntax.Module) extends Interpreter:
  final type I32 = ReducedProduct[Topped[Int], FunctionPointers]
  final type I64 = Topped[Long]
  final type F32 = Topped[Float]
  final type F64 = Topped[Double]
  final type Bool = Topped[Boolean]

  given Join[I32] = new CombineReducedProduct[Topped[Int], FunctionPointers, Widening.No]
                                             (using new CombineToppedFlat,
                                                    new CombineToppedDeep,
                                                    ReduceConstIntFunctionPointer(getModule))

  given Join[I64] = new CombineToppedFlat
  given Join[F32] = new CombineToppedFlat
  given Join[F64] = new CombineToppedFlat
  given Join[Bool] = new CombineToppedFlat

  given ConstIntFunctionPointerIntOps: IntegerOps[Int,I32] = implicitly

  final def topI32: I32 = ReducedProduct(Topped.Top, Topped.Top)
  final def topI64: I64 = Topped.Top
  final def topF32: F32 = Topped.Top
  final def topF64: F64 = Topped.Top

  final def asBoolean(v: Value)(using Failure): Bool = v.asInt32 match
    case ReducedProduct(Topped.Top,_) => Topped.Top
    case ReducedProduct(Topped.Actual(i),_) => Topped.Actual(i != 0)
  final def boolean(b: Bool): Value = b match
    case Topped.Top => Value.Int32(topI32)
    case Topped.Actual(true) => Value.Int32(summon[IntegerOps[Int,I32]].integerLit(1))
    case Topped.Actual(false) => Value.Int32(summon[IntegerOps[Int,I32]].integerLit(0))

  def constantInstructions(analysis: Instance): ConstantInstructionsLogger =
    val constants = new ConstantInstructionsLogger(analysis.stack)(using analysis.failure)
    analysis.fixpoint.addContextFreeLogger(constants)
    constants

  class ConstantInstructionsLogger(stack: DecidableOperandStack[Value])(using Failure) extends InstructionResultLogger[Value](stack):
    override def boolValue(v: Value): Value = boolean(asBoolean(v))
    override def dummyValue: Value = Value.Int32(summon[IntegerOps[Int,I32]].integerLit(0))

    def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
      case Value.TopValue => false
      case Value.Int32(v) => v._1.isActual
      case Value.Int64(v) => v.isActual
      case Value.Float32(v) => v.isActual
      case Value.Float64(v) => v.isActual
    })

    def grouped: Map[String, Map[InstLoc, List[Value]]] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

    def groupedCount: Map[String, Int] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap

final class ReduceConstIntFunctionPointer(getModule: => swam.syntax.Module) extends Reduce[Topped[Int], FunctionPointers]:
  extension (p: ReducedProduct[Topped[Int], FunctionPointers])
    def reduce: ReducedProduct[Topped[Int], FunctionPointers] =
      p match
        case ReducedProduct(Topped.Actual(x), _) =>
          getFunction(x) match
            case Some(fun) => ReducedProduct(Topped.Actual(x), Topped.Actual(Powerset(fun)))
            case None => p
        case ReducedProduct(Topped.Top, funs@Topped.Actual(ps)) if ps.size == 1 =>
          ReducedProduct(Topped.Actual(ps(0).funcId), funs)
        case _ => p
