//package sturdy.effect.failure
//
//import sturdy.values.Powerset
//
//enum CFallible[T]:
//  case Unfailing(t: T)
//  case Failing(kind: FailureKind, msg: String)
//
//  def isFailing: Boolean = this match
//    case Unfailing(_) => false
//    case Failing(_, _) => true
//
//  def get: T = this match
//    case Unfailing(t) => t
//    case _ => throw new MatchError(this)
