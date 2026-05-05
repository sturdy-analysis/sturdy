package sturdy.language.wasm.abstractions

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{FixOut, JumpTarget, WasmException}
import sturdy.util.Lazy
import sturdy.values.{Join, Widen}
import sturdy.values.exceptions.{Exceptional, ExceptionalByTarget}

trait ExceptionByTarget extends Interpreter:
  final type ExcV = Map[JumpTarget, ExceptionState]
  case class ExceptionState(operands: List[Value], effectState: Any)

  given JoinExceptionState(using lazyEffectStack: Lazy[EffectStack], joinValue: Join[Value]): Join[ExceptionState] =
    (state1, state2) =>
      lazyEffectStack.value.joinOut[List[Value]](FixOut.Exception())((state1.operands, state1.effectState), (state2.operands, state2.effectState)).map(ExceptionState.apply)

  given WidenExceptionState(using lazyEffectStack: Lazy[EffectStack], widenValue: Widen[Value]): Widen[ExceptionState] =
    (state1, state2) =>
      lazyEffectStack.value.widenOut[List[Value]](FixOut.Exception())((state1.operands, state1.effectState), (state2.operands, state2.effectState)).map(ExceptionState.apply)
      
  given Exceptional[WasmException[Value], ExcV, WithJoin] = ExceptionalByTarget[WasmException[Value], JumpTarget, ExceptionState](
    e => (e.target, ExceptionState(e.operands, e.state)),
    (target,state) => WasmException(target, state.operands, state.effectState)
  )

