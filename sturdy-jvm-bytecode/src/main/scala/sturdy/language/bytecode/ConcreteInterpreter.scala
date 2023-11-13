package sturdy.language.bytecode

import sturdy.data.{*, given}
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.{ConcreteExcept, Except}
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.language.bytecode.Interpreter
import sturdy.language.bytecode.generic.*
import sturdy.values.booleans.{BooleanBranching, ConcreteBooleanBranching}
import sturdy.values.exceptions.ConcreteExceptional
import sturdy.values.floating.FloatOps
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}

object ConcreteInterpreter extends Interpreter:
  //override type I8  = Byte
  //override type I16 = Short
  override type I32 = Int
  override type I64 = Long
  override type F32 = Float
  override type F64 = Double
  override type Bool = Boolean

  //override def topI8: Byte = throw new UnsupportedOperationException
  //override def topI16: Short = throw new UnsupportedOperationException
  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException

  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0

  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)
  class Instance extends GenericInstance:
    val newFrameData: FrameData = ()
    val args: List[Value] = List()

    val joinUnit: MayJoin.NoJoin[FrameData] = implicitly

    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val failure: ConcreteFailure = new ConcreteFailure
    val frame: ConcreteCallFrame[FrameData, Int, Value] = new ConcreteCallFrame[FrameData, Int, Value](newFrameData, args.view.zipWithIndex.map(_.swap))
    val except: Except[JvmExcept, JvmExcept, MayJoin.NoJoin] = new ConcreteExcept


    private given Failure = failure

    val bytecodeOps: BytecodeOps[Value] = implicitly


