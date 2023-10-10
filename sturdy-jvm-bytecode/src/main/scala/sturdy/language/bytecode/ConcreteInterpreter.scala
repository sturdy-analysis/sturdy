package sturdy.language.bytecode

import sturdy.data.{*, given}
import sturdy.effect.failure.{ConcreteFailure, Failure}
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.language.bytecode.Interpreter
import sturdy.language.bytecode.generic.*
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

  //override def topI8: Byte = throw new UnsupportedOperationException
  //override def topI16: Short = throw new UnsupportedOperationException
  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException

  class Instance extends GenericInstance:
    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val failure: ConcreteFailure = new ConcreteFailure

    private given Failure = failure

    val bytecodeOps: BytecodeOps[Value] = implicitly


