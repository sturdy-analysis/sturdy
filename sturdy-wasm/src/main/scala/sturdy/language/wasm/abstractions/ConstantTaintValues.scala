package sturdy.language.wasm.abstractions

import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.OperandStack
import sturdy.language.wasm.ConcreteInterpreter
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.analyses.ConstantAnalysis
import sturdy.values.Finite
import sturdy.values.Topped
import sturdy.values.taint.*
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.{FixIn, FixOut, InstLoc}
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

  def taintedMemoryAccessLogger()(using stack: OperandStack[Value]): TaintedMemoryAccessLogger = new TaintedMemoryAccessLogger

  class TaintedMemoryAccessLogger(using stack: OperandStack[Value]) extends fix.Logger[FixIn[Value], FixOut[Value]]:
    var taintedMemoryAccesses: Map[InstLoc, syntax.Inst] = Map()
    var taintedMemoryAddresses: Map[InstLoc, Set[Value]] = Map()

    override def enter(dom: FixIn[Value]): Unit = dom match
      case FixIn.Eval(inst, loc) =>
        if (isMemoryLoadStoreInstruction(inst))
          val address = {
            if (addressOnTopOfStack(inst))
              stack.peek()
            else
              val v1 = stack.pop()
              val a = stack.peek()
              stack.push(v1)
              a
          }
          if (maybeTainted(address))
            addInstruction(inst, loc, address)
      case _ => // nothing

    override def exit(dom: FixIn[Value], codom: TrySturdy[FixOut[Value]]): Unit = ()

    def addInstruction(inst: syntax.Inst, loc: InstLoc, v: Value): Unit =
      taintedMemoryAccesses = taintedMemoryAccesses + (loc -> inst)
      taintedMemoryAddresses.get(loc) match
        case None =>
          taintedMemoryAddresses = taintedMemoryAddresses + (loc -> Set(v))
        case Some(previousResult) =>
          val newVals = previousResult + v
          taintedMemoryAddresses = taintedMemoryAddresses + (loc -> newVals)

    def addressOnTopOfStack(inst: syntax.Inst): Boolean = inst match
      case _: syntax.LoadInst => true
      case _: syntax.LoadNInst => true
      case _ => false

    def isMemoryLoadStoreInstruction(inst: syntax.Inst): Boolean =
      inst.opcode >= OpCode.I32Load && inst.opcode <= OpCode.I64Store32

    def maybeTainted(v: Value): Boolean = v match
      case Value.TopValue => true
      case Value.Int32(TaintProduct(Taint.TopTaint,_)) => true
      case Value.Int32(TaintProduct(Taint.Tainted,_)) => true
      case Value.Int64(TaintProduct(Taint.TopTaint,_)) => true
      case Value.Int64(TaintProduct(Taint.Tainted,_)) => true
      case Value.Float32(TaintProduct(Taint.TopTaint,_)) => true
      case Value.Float32(TaintProduct(Taint.Tainted,_)) => true
      case Value.Float64(TaintProduct(Taint.TopTaint,_)) => true
      case Value.Float64(TaintProduct(Taint.Tainted,_)) => true
      case _ => false