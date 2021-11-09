package sturdy.language.wasm

import sturdy.data.unit
import sturdy.effect.except.Except
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.language.wasm.generic.FrameData
import sturdy.language.wasm.generic.UnreachableInstruction
import sturdy.language.wasm.generic.WasmException
import sturdy.values.booleans.BooleanBranching
import sturdy.values.floating.FloatingOps
import sturdy.values.integer.IntegerOps
import sturdy.values.relational.EqOps
import swam.syntax.*


trait SimpleGenericInterpreter[V, ExcV, MayJoin[_]]:
  /** Effects are stacked so that their behavior gets interleaved. */
  val effects: DecidableOperandStack[V] & Except[WasmException[V], ExcV, MayJoin]

  val f32ops: FloatingOps[Float, V]
  val f64ops: FloatingOps[Double, V]

  def evalInst(inst: Inst): Unit = inst match
    case f32.Sub =>
      val v2 = effects.popOrFail()
      val v1 = effects.popOrFail()
      effects.push(f32ops.sub(v1,v2))
    case f64.Sqrt =>
      val v = effects.popOrFail()
      effects.push(f64ops.sqrt(v))
    case Return =>
      val operands = effects.popNOrFail(getFrameData.returnArity)
      effects.throws(WasmException.Return(operands))
    case _ => ???


  private given Failure = ???
  def getFrameData: FrameData = ???
