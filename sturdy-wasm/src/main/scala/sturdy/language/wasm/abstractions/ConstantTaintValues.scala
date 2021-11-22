package sturdy.language.wasm.abstractions

import sturdy.data.{CombineEquiList, noJoin}
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.values.Finite
import sturdy.values.Topped
import sturdy.values.taint.{*, given}
import sturdy.values.booleans.given
import sturdy.values.floating.given
import sturdy.values.integer.given
import sturdy.values.given
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.{FixIn, FixOut, InstLoc}
import sturdy.values.Powerset
import swam.syntax.Inst
import swam.{OpCode, syntax}

import scala.collection.MapView
import java.security.KeyStore.TrustedCertificateEntry

trait ConstantTaintValues extends Interpreter:
  final type I32 = TaintProduct[Topped[Int]]
  final type I64 = TaintProduct[Topped[Long]]
  final type F32 = TaintProduct[Topped[Float]]
  final type F64 = TaintProduct[Topped[Double]]
  final type Bool = TaintProduct[Topped[Boolean]]

  final def topI32: I32 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topI64: I64 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topF32: F32 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topF64: F64 = TaintProduct(Taint.TopTaint, Topped.Top)

  def getTaint(v: Value): Taint = v match
    case Value.TopValue => Taint.TopTaint
    case Value.Int32(tp) => tp.taint
    case Value.Int64(tp) => tp.taint
    case Value.Float32(tp) => tp.taint
    case Value.Float64(tp) => tp.taint

  final def asBoolean(v: Value)(using Failure): Bool = v.asInt32.map {
    case Topped.Top => Topped.Top
    case Topped.Actual(i) => Topped.Actual(i != 0)
  }
  final def boolean(b: Bool): Value = Value.Int32(b.map {
    case Topped.Top => Topped.Top
    case Topped.Actual(true) => Topped.Actual(1)
    case Topped.Actual(false) => Topped.Actual(0)
  })

  def liftConcreteValue(cv: ConcreteInterpreter.Value, taint: Taint = Taint.Untainted): Value =
    cv match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(injectTaint(taint, Topped.Actual(i)))
      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(injectTaint(taint, Topped.Actual(l)))
      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(injectTaint(taint, Topped.Actual(f)))
      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(injectTaint(taint, Topped.Actual(d)))

  def liftConstantValue(cv: ConstantAnalysis.Value, taint: Taint = Taint.Untainted): Value =
    cv match
      case ConstantAnalysis.Value.TopValue => Value.TopValue
      case ConstantAnalysis.Value.Int32(i) => Value.Int32(injectTaint(taint,i))
      case ConstantAnalysis.Value.Int64(l) => Value.Int64(injectTaint(taint,l))
      case ConstantAnalysis.Value.Float32(f) => Value.Float32(injectTaint(taint,f))
      case ConstantAnalysis.Value.Float64(d) => Value.Float64(injectTaint(taint,d))

  def untaint(v: Value): ConstantAnalysis.Value = v match
    case Value.TopValue => ConstantAnalysis.Value.TopValue
    case Value.Int32(TaintProduct(_, v1)) => ConstantAnalysis.Value.Int32(v1)
    case Value.Int64(TaintProduct(_, v1)) => ConstantAnalysis.Value.Int64(v1)
    case Value.Float32(TaintProduct(_, v1)) => ConstantAnalysis.Value.Float32(v1)
    case Value.Float64(TaintProduct(_, v1)) => ConstantAnalysis.Value.Float64(v1)

  def constantInstructions(analysis: Instance): ConstantInstructionsLogger =
    val constants = new ConstantInstructionsLogger(analysis.stack)(using analysis.failure)
    analysis.addContextFreeLogger(constants)
    constants

  class ConstantInstructionsLogger(stack: DecidableOperandStack[Value])(using Failure) extends InstructionResultLogger[Value](stack):
    override def boolValue(v: Value): Value = boolean(asBoolean(v))
    override def dummyValue: Value = Value.Int32(TaintProduct(Taint.Untainted, Topped.Actual(0)))

    def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
      case Value.TopValue => false
      case Value.Int32(TaintProduct(_, Topped.Top)) => false
      case Value.Int64(TaintProduct(_, Topped.Top)) => false
      case Value.Float32(TaintProduct(_, Topped.Top)) => false
      case Value.Float64(TaintProduct(_, Topped.Top)) => false
      case _ => true
    })

    def grouped: Map[String, Map[InstLoc, List[Value]]] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

    def groupedCount: Map[String, Int] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap


  def taintedMemoryAccessLogger(analysis: Instance): TaintedMemoryAccessLogger = {
    val logger = new TaintedMemoryAccessLogger(analysis.stack)(using analysis.failure)
    analysis.addContextFreeLogger(logger)
    logger
  }

  class TaintedMemoryAccessLogger(stack: DecidableOperandStack[Value])(using Failure) extends InstructionLogger[Powerset[Value], Value]:
    override def enterInfo(inst: Inst): Option[Powerset[Value]] =
      if (isMemoryLoadStoreInstruction(inst)) {
        val address =
          if (addressOnTopOfStack(inst))
            stack.peekOrFail()
          else
            stack.peekNOrFail(2).tail.head

        if (maybeTainted(address))
          Some(Powerset(address))
        else
          None
      } else
        None

    override def exitInfo(inst: Inst, success: Boolean): Option[Powerset[Value]] = None

    def addressOnTopOfStack(inst: syntax.Inst): Boolean = inst match
      case _: syntax.LoadInst => true
      case _: syntax.LoadNInst => true
      case _ => false

    inline def isMemoryLoadStoreInstruction(inst: syntax.Inst): Boolean =
      inst.opcode >= OpCode.I32Load && inst.opcode <= OpCode.I64Store32

    def maybeTainted(v: Value): Boolean =
      Taint.Tainted <= getTaint(v)
