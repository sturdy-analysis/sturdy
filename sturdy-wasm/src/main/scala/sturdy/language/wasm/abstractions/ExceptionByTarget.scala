package sturdy.language.wasm.abstractions

import sturdy.data.WithJoin
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{JumpTarget, WasmException}
import sturdy.values.exceptions.{Exceptional, ExceptionalByTarget}
import swam.LabelIdx

trait ExceptionByTarget extends Interpreter:
  final type ExcV = Map[JumpTarget, List[Value]]

  given Exceptional[WasmException[Value], ExcV, WithJoin] =
    new ExceptionalByTarget(e => (e.target, e.operands), WasmException.apply)

