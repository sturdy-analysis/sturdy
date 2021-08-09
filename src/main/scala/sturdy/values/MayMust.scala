package sturdy.values

enum MayMust[T]:
  case May(t: T)
  case Must(t: T)

  def get: T = this match
    case May(t) => t
    case Must(t) => t

  def isMust: Boolean = this match
    case Must(_) => true
    case May(_) => false

  def map[U](f: T => U): MayMust[U] = this match
    case May(t) => May(f(t))
    case Must(t) => Must(f(t))

given finiteMayMust[T](using Finite[T]): Finite[MayMust[T]] with {}

given joinMayMust[T](using j: JoinValue[T]): JoinValue[MayMust[T]] with
  override def joinValues(v1: MayMust[T], v2: MayMust[T]): MayMust[T] = (v1, v2) match
    case (MayMust.Must(t1), MayMust.Must(t2)) => MayMust.Must(j.joinValues(t1, t2))
    case _ => MayMust.May(j.joinValues(v1.get, v2.get))

given mayMustPO[T](using po: PartialOrder[T]): PartialOrder[MayMust[T]] with
  override def lteq(x: MayMust[T], y: MayMust[T]): Boolean =
    if (!x.isMust && y.isMust)
      false
    else
      po.lteq(x.get, y.get)
