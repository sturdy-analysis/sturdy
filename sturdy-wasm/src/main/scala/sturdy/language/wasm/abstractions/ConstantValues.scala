package sturdy.language.wasm.abstractions

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
import swam.syntax.BrIf

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
    var brifInstructions: Map[InstLoc, Topped[Boolean]] = Map()
    var numericInstructions: Map[InstLoc, Value] = Map()

//    def getConstantBrifInstructions: Map[InstLoc, Boolean] =
//      brifInstructions.flatten

    def getConstantNumericInstructions: Map[InstLoc, Value] = numericInstructions.filter(_._2 match
      case Value.TopValue => false
      case Value.Int32(Topped.Top) => false
      case Value.Int64(Topped.Top) => false
      case Value.Float32(Topped.Top) => false
      case Value.Float64(Topped.Top) => false
      case _ => true
    )

    override def enter(dom: FixIn[Value]): Unit = dom match
      case FixIn.Eval(BrIf(_), loc) =>
        val doJump = stack.peek() match
          case Value.Int32(Topped.Actual(i)) => Topped.Actual(i != 0)
          case _ => Topped.Top
        brifInstructions.get(loc) match
          case None =>
            brifInstructions += loc -> doJump
          case Some(previousResult) =>
            val joined = Join(previousResult, doJump).get
            brifInstructions += loc -> joined

    override def exit(dom: FixIn[Value], codom: TrySturdy[FixOut[Value]]): Unit = dom match
      case FixIn.Eval(inst, loc) =>
        val opcode = inst.opcode
        if (opcode >= OpCode.I32Const && opcode <= OpCode.I64Extend32S) {
          val result = if (codom.isSuccess) stack.peek() else Value.TopValue
          numericInstructions.get(loc) match
            case None =>
              numericInstructions += loc -> result
            case Some(previousResult) =>
              val joined = Join(previousResult, result).get
              numericInstructions += loc -> joined
        }
      case _ => // nothing
