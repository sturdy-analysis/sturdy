package sturdy.language.wasm.svbenchc

import sturdy.language.wasm.generic.{HostFunction}
import swam.FuncType
import swam.ValType.*

val svbenchHostFunctions: List[HostFunction] = List(
  HostFunction("__VERIFIER_nondet_bool", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_char", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_short", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_int", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_long", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_longlong", FuncType(Vector(), Vector(I64))),
  HostFunction("__VERIFIER_nondet_uchar", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_ushort", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_uint", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_ulong", FuncType(Vector(), Vector(I32))),
  HostFunction("__VERIFIER_nondet_ulonglong", FuncType(Vector(), Vector(I64))),
  HostFunction("__VERIFIER_nondet_float", FuncType(Vector(), Vector(F32))),
  HostFunction("__VERIFIER_nondet_double", FuncType(Vector(), Vector(F64))),
  HostFunction("host_assert_fail", FuncType(Vector(), Vector())),
  HostFunction("__assert_fail", FuncType(Vector(I32, I32, I32, I32), Vector())),
)

