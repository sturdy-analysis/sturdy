package sturdy.language.wasm.abstractions

import sturdy.data.WithJoin
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.{JumpTarget, WasmException}
import sturdy.values.{PathSensitive, assertPath}
import sturdy.values.exceptions.{Exceptional, ExceptionalByTarget}
import swam.LabelIdx

trait ExceptionByTarget extends Interpreter:
  final type ExcV = Map[JumpTarget, List[Value]]

  given Exceptional[WasmException[Value], ExcV, WithJoin] =
    new ExceptionalByTarget(e => (e.target, e.operands), WasmException.apply)

  given PathSensitiveExc(using PathSensitive[I32], PathSensitive[I64], PathSensitive[F32], PathSensitive[F64]): PathSensitive[ExcV] with
    override def assert(cond: Any, v: Map[JumpTarget, List[Value]]): Map[JumpTarget, List[Value]] =
      v.view.mapValues(_.map(_.assertPath(cond))).toMap