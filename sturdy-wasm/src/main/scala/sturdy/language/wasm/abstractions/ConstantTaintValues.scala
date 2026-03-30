package sturdy.language.wasm.abstractions

import sturdy.data.{*, given}
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.OperandStack
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.values.{*, given}
import sturdy.values.taint.{*, given}
import sturdy.values.booleans.given
import sturdy.values.floating.given
import sturdy.values.integer.given
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.{FixIn, FixOut, FunctionInstance, InstLoc}
import sturdy.values.references.ReferenceOps
import swam.syntax.Inst
import swam.{OpCode, syntax}

import scala.collection.MapView
import java.security.KeyStore.TrustedCertificateEntry


trait ConstantTaintValues extends Interpreter:
  final type I32 = TaintProduct[Topped[Int]]
  final type I64 = TaintProduct[Topped[Long]]
  final type F32 = TaintProduct[Topped[Float]]
  final type F64 = TaintProduct[Topped[Double]]
  final type V128 = TaintProduct[Topped[Array[Byte]]]
  final type Bool = TaintProduct[Topped[Boolean]]
  final type Reference = TaintProduct[Powerset[FunctionInstance | ExternReference]]
  final type RefV = Reference
  final type FunV = Powerset[FunctionInstance]

  final def topI32: I32 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topI64: I64 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topF32: F32 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topF64: F64 = TaintProduct(Taint.TopTaint, Topped.Top)
  final def topV128: V128 = TaintProduct(Taint.TopTaint, Topped.Top)

  def getTaint(v: Value): Taint = v match
    case Value.TopValue => Taint.TopTaint
    case Value.Num(NumValue.Int32(tp)) => tp.taint
    case Value.Num(NumValue.Int64(tp)) => tp.taint
    case Value.Num(NumValue.Float32(tp)) => tp.taint
    case Value.Num(NumValue.Float64(tp)) => tp.taint
    case Value.ExnRef(_) => Taint.Untainted
    case Value.Ref(tp) => tp.asInstanceOf[TaintProduct[Powerset[FunctionInstance | ExternReference]]].taint
    case Value.Vec(tp) => tp.taint

  final def asBoolean(v: Value)(using Failure): Bool = v.asInt32.map {
    case Topped.Top => Topped.Top
    case Topped.Actual(i) => Topped.Actual(i != 0)
  }
  final def booleanToVal(b: Bool): Value = Value.Num(NumValue.Int32(b.map {
    case Topped.Top => Topped.Top
    case Topped.Actual(true) => Topped.Actual(1)
    case Topped.Actual(false) => Topped.Actual(0)
  }))

  def liftConcreteValue(cv: ConcreteInterpreter.Value, taint: Taint = Taint.Untainted): Value =
    cv match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(i)) => Value.Num(NumValue.Int32(injectTaint(taint, Topped.Actual(i))))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(l)) => Value.Num(NumValue.Int64(injectTaint(taint, Topped.Actual(l))))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => Value.Num(NumValue.Float32(injectTaint(taint, Topped.Actual(f))))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => Value.Num(NumValue.Float64(injectTaint(taint, Topped.Actual(d))))

  def liftConstantValue(cv: ConstantAnalysis.Value, taint: Taint = Taint.Untainted): Value =
    cv match
      case ConstantAnalysis.Value.TopValue => Value.TopValue
      case ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Int32(i)) => Value.Num(NumValue.Int32(injectTaint(taint,i)))
      case ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Int64(l)) => Value.Num(NumValue.Int64(injectTaint(taint,l)))
      case ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Float32(f)) => Value.Num(NumValue.Float32(injectTaint(taint,f)))
      case ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Float64(d)) => Value.Num(NumValue.Float64(injectTaint(taint,d)))

  def untaint(v: Value): ConstantAnalysis.Value = v match
    case Value.TopValue => ConstantAnalysis.Value.TopValue
    case Value.Num(NumValue.Int32(TaintProduct(_, v1))) => ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Int32(v1))
    case Value.Num(NumValue.Int64(TaintProduct(_, v1))) => ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Int64(v1))
    case Value.Num(NumValue.Float32(TaintProduct(_, v1))) => ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Float32(v1))
    case Value.Num(NumValue.Float64(TaintProduct(_, v1))) => ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Float64(v1))

  def constantInstructions(analysis: Instance): ConstantInstructionsLogger =
    val constants = new ConstantInstructionsLogger(analysis.stack)(using analysis.failure)
    analysis.fixpoint.addContextFreeLogger(constants)
    constants

  class ConstantInstructionsLogger(stack: OperandStack[Value, NoJoin])(using Failure) extends InstructionResultLogger[Value, Value](stack):
    override def boolValue(v: Value): Value = booleanToVal(asBoolean(v))
    override def getInfo(v: Value): Value = v

    def get: Map[InstLoc, List[Value]] = instructionInfo.filter(_._2.forall {
      case Value.TopValue => false
      case Value.Num(NumValue.Int32(TaintProduct(_, v))) => v.isActual
      case Value.Num(NumValue.Int64(TaintProduct(_, v))) => v.isActual
      case Value.Num(NumValue.Float32(TaintProduct(_, v))) => v.isActual
      case Value.Num(NumValue.Float64(TaintProduct(_, v))) => v.isActual
      case Value.Ref(TaintProduct(_, v)) => v.size == 1
      case Value.Vec(TaintProduct(_, v)) => v.isActual
      case _ => true
    })

    def grouped: Map[String, Map[InstLoc, List[Value]]] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

    def groupedCount: Map[String, Int] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap


  def taintedMemoryAccessLogger(analysis: Instance): TaintedMemoryAccessLogger = {
    val logger = new TaintedMemoryAccessLogger(analysis.stack)(using analysis.failure)
    analysis.fixpoint.addContextFreeLogger(logger)
    logger
  }

  class TaintedMemoryAccessLogger(stack: OperandStack[Value, MayJoin.NoJoin])(using Failure) extends InstructionLogger[Powerset[Value], Value]:
    def memoryInstructions: Map[InstLoc, Powerset[Value]] = instructionInfo
    def taintedMemoryInstructions: Map[InstLoc, Powerset[Value]] = instructionInfo.filter(_._2.set.nonEmpty)
    
    override def enterInfo(inst: Inst, loc: InstLoc): Option[Powerset[Value]] =
      if (isMemoryLoadStoreInstruction(inst)) {
        val address =
          if (addressOnTopOfStack(inst))
            stack.peekOrFail()
          else
            stack.peekNOrFail(2).tail.head

        if (maybeTainted(address))
          Some(Powerset(address))
        else
          Some(Powerset())
      } else
        None

    override def exitInfo(inst: Inst, loc: InstLoc, success: Boolean): Option[Powerset[Value]] = None

    def addressOnTopOfStack(inst: syntax.Inst): Boolean = inst match
      case _: syntax.LoadInst => true
      case _: syntax.LoadNInst => true
      case _ => false

    inline def isMemoryLoadStoreInstruction(inst: syntax.Inst): Boolean =
      inst.opcode >= OpCode.I32Load && inst.opcode <= OpCode.I64Store32

    def maybeTainted(v: Value): Boolean =
      Taint.Tainted <= getTaint(v)

  given CombineReference[W <: Widening](using Combine[TaintProduct[Powerset[FunctionInstance | ExternReference]], W]): Combine[Reference, W] with
    override def apply(r1: Reference, r2: Reference): MaybeChanged[Reference] = (r1, r2) match
      case (tp1: TaintProduct[?], tp2: TaintProduct[?]) =>
        summon[Combine[TaintProduct[Powerset[FunctionInstance | ExternReference]], W]](
          tp1.asInstanceOf[TaintProduct[Powerset[FunctionInstance | ExternReference]]],
          tp2.asInstanceOf[TaintProduct[Powerset[FunctionInstance | ExternReference]]]
        ).asInstanceOf[MaybeChanged[Reference]]
      case _ =>
        throw CannotJoinException(s"Cannot join $r1 and $r2")

  given TaintReference: ReferenceOps[FunV, RefV] with {
    override def mkNullRef: RefV = ???
    override def mkExternNullRef: RefV = ???
    override def mkRef(trg: FunV): RefV = ???
    override def deref(v: RefV): FunV = ???
  }