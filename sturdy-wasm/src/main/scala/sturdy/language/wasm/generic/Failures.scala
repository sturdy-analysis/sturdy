package sturdy.language.wasm.generic

import sturdy.effect.failure.FailureKind

case object UnreachableInstruction extends FailureKind
case object UnboundLocal extends FailureKind
case object UnboundGlobal extends FailureKind
case object UnboundFunctionType extends FailureKind
case object UnboundFunctionIndex extends FailureKind
case object UninitializedFunction extends FailureKind
case object IndirectCallTypeMismatch extends FailureKind
case object MemoryAccessOutOfBounds extends FailureKind