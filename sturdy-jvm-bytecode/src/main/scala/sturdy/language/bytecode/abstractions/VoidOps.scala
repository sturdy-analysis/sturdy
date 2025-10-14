package sturdy.language.bytecode.abstractions

// void is currently represented through a unit value for easier handling
trait VoidOps[V]:
  // the value used for void
  val voidRep: V
