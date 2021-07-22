package sturdy.effect.failure

import sturdy.values.Powerset

enum CFallible[T]:
  case Unfailing(t: T)
  case Failing(kind: FailureKind, msg: String)
