package sturdy.language.jimple

import sturdy.effect.failure.FailureKind

object UnboundLocal extends FailureKind
object UnboundClass extends FailureKind
object UnboundField extends FailureKind
object ClassNotLoaded extends FailureKind
object MethodNotLoaded extends FailureKind
object TypeError extends FailureKind