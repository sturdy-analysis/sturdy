package sturdy.language.wasm.abstractions

import sturdy.data.CombineEquiList
import sturdy.effect.TrySturdy
import sturdy.effect.failure.Failure
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.fix
import sturdy.fix.Logger
import sturdy.ir.IR
import sturdy.language.wasm.generic.{FixIn, FixOut, InstLoc}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.booleans.given
import sturdy.values.floating.given
import sturdy.values.{Finite, Join, Topped, given}
import sturdy.values.integer.given
import swam.{OpCode, syntax}
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import scala.collection.MapView

trait IRValues extends Interpreter:
  final type I32 = IR
  final type I64 = IR
  final type F32 = IR
  final type F64 = IR
  final type Bool = IR

  final def topI32: I32 = IR.Unknown()
  final def topI64: I64 = IR.Unknown()
  final def topF32: F32 = IR.Unknown()
  final def topF64: F64 = IR.Unknown()

  final def asBoolean(v: Value)(using Failure): Bool = v.asInt32
  final def boolean(b: Bool): Value = Value.Int32(b)
