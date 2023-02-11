package sturdy.language.jimple

import sturdy.effect.failure.FailureKind

// Possible failure kinds in jimple analyses

object UnboundLocal extends FailureKind
object UnboundClass extends FailureKind
object UnboundField extends FailureKind
object UnboundGlobal extends FailureKind
object ClassNotLoaded extends FailureKind
object MethodNotLoaded extends FailureKind
object RuntimeUnitNotLoaded extends FailureKind
object TypeError extends FailureKind