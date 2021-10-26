package sturdy.language.wasm.abstractions

import sturdy.data.CombineEquiList
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.OperandStack
import sturdy.fix
import sturdy.fix.Logger
import sturdy.language.wasm.generic.FixIn
import sturdy.language.wasm.generic.FixOut
import sturdy.language.wasm.generic.InstLoc
import sturdy.language.wasm.{Interpreter, ConcreteInterpreter}
import sturdy.values.Finite
import sturdy.values.Join
import sturdy.values.Topped
import sturdy.values.booleans.given
import sturdy.values.doubles.given
import sturdy.values.floats.given
import sturdy.values.ints.given
import sturdy.values.longs.given
import sturdy.values.given
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

  def liftConcreteValue(cv: ConcreteInterpreter.Value): Value = cv match
    case ConcreteInterpreter.Value.TopValue => Value.TopValue
    case ConcreteInterpreter.Value.Int32(i) => Value.Int32(Topped.Actual(i))
    case ConcreteInterpreter.Value.Int64(l) => Value.Int64(Topped.Actual(l))
    case ConcreteInterpreter.Value.Float32(f) => Value.Float32(Topped.Actual(f))
    case ConcreteInterpreter.Value.Float64(d) => Value.Float64(Topped.Actual(d))

  def constantInstructionsLogger()(using stack: OperandStack[Value]): ConstantInstructionsLogger = new ConstantInstructionsLogger

  class ConstantInstructionsLogger(using stack: OperandStack[Value]) extends fix.Logger[FixIn[Value], FixOut[Value]]:
    var instructionValues: Map[InstLoc, List[Value]] = Map()
    var instructions: Map[InstLoc, syntax.Inst] = Map()

    private def addInstruction(inst: syntax.Inst, loc: InstLoc, v: List[Value]): Unit =
      instructions += loc -> inst
      instructionValues.get(loc) match
        case None =>
          instructionValues += loc -> v
        case Some(previousResult) =>
          val joined = Join(previousResult, v).get
          instructionValues += loc -> joined

    def get: Map[InstLoc, List[Value]] = instructionValues.filter(_._2.forall {
      case Value.TopValue => false
      case Value.Int32(Topped.Top) => false
      case Value.Int64(Topped.Top) => false
      case Value.Float32(Topped.Top) => false
      case Value.Float64(Topped.Top) => false
      case _ => true
    })

    def grouped: Map[String, Map[InstLoc, List[Value]]] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName)

    def groupedCount: Map[String, Int] =
      get.groupBy(kv => instructions(kv._1).getClass.getSimpleName).view.mapValues(_.size).toMap

    override def enter(dom: FixIn[Value]): Unit = dom match
      case FixIn.Eval(inst, loc) =>
        if (readsSingleValueFromStack(inst)) {
          val value = stack.peek()
          addInstruction(inst, loc, List(value))
        } else if (readsSingleBooleanFromStack(inst)) {
          val boolValue = stack.peek() match
            case Value.Int32(Topped.Actual(i)) => boolean(Topped.Actual(i != 0))
            case _ => boolean(Topped.Top)
          addInstruction(inst, loc, List(boolValue))
        } else inst match {
          case _: StoreInst | _: StoreNInst =>
            val values = stack.peekN(2)
            addInstruction(inst, loc, values)
          case _ => // nothing
        }
      case _ => // nothing

    override def exit(dom: FixIn[Value], codom: TrySturdy[FixOut[Value]]): Unit = dom match
      case FixIn.Eval(inst, loc) =>
        if (inst == syntax.Nop || inst == syntax.Unreachable) {
          addInstruction(inst, loc, List(Value.Int32(Topped.Actual(0))))
        } else if (writesSingleValueToStack(inst)) {
          val result = if (codom.isSuccess) stack.peek() else Value.TopValue
          addInstruction(inst, loc, List(result))
        } else inst match {
          case _: LoadInst | _: LoadNInst =>
            val loaded = stack.peek()
            val values = List(loaded)
            addInstruction(inst, loc, values)
          case _ => // nothing
        }
      case _ => // nothing

    def writesSingleValueToStack(inst: syntax.Inst): Boolean =
      val opcode = inst.opcode
      if (opcode >= OpCode.I32Eqz && opcode <= OpCode.I64Extend32S)
        return true

      inst match {
        case _: syntax.LocalGet | _: syntax.GlobalGet => true
        case _: syntax.MemorySize.type | _: syntax.LoadInst | _: syntax.LoadNInst => true
        case _: syntax.Miscop => true
        case _ => false
      }

    def readsSingleValueFromStack(inst: syntax.Inst): Boolean = inst match
      case _: syntax.LocalSet | _: syntax.GlobalSet | _: syntax.LocalTee => true
      case _: syntax.StoreInst | _: syntax.StoreNInst => true
      case _: syntax.Select.type => true
      case _: syntax.BrTable | _: syntax.CallIndirect => true
      case _ => false

    def readsSingleBooleanFromStack(inst: syntax.Inst): Boolean = inst match
      case _: syntax.If | _: syntax.BrIf => true
      case _: syntax.Select.type => true
      case _ => false

