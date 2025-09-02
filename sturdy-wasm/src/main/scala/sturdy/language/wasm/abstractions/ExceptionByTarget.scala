package sturdy.language.wasm.abstractions

import sturdy.data.WithJoin
import sturdy.effect.EffectStack
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{BreakIfState, JumpTarget, WasmException}
import sturdy.values.{Combine, Join, MaybeChanged, Widen, Widening}
import sturdy.values.exceptions.{Exceptional, ExceptionalByTarget}
import swam.LabelIdx

trait ExceptionByTarget extends Interpreter:
  final type ExcV = Map[JumpTarget, (List[Value],Option[BreakIfState[Value]])]

  given ExceptByTarget: Exceptional[WasmException[Value], ExcV, WithJoin] =
    new ExceptionalByTarget(e => (e.target, (e.operands,e.breakIfState)), { case (trg, (ops, breakIfState)) => WasmException(trg, ops, breakIfState) })

  given JoinBreakIfState(using combineValue: Join[Value], effectStack: EffectStack): Join[BreakIfState[Value]] with
    override def apply(v1: BreakIfState[Value], v2: BreakIfState[Value]): MaybeChanged[BreakIfState[Value]] =
      for {
        condition <- combineValue(v1.condition, v2.condition)
        state <- effectStack.join(v1.state, v2.state)
      } yield(BreakIfState(condition, state))

  given WidenBreakIfState(using combineValue: Widen[Value], effectStack: EffectStack): Widen[BreakIfState[Value]] with
    override def apply(v1: BreakIfState[Value], v2: BreakIfState[Value]): MaybeChanged[BreakIfState[Value]] =
      for {
        condition <- combineValue(v1.condition, v2.condition)
        state <- effectStack.widen(v1.state, v2.state)
      } yield(BreakIfState(condition, state))