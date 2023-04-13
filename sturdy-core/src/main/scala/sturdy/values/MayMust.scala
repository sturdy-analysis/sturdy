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

  def asMay: MayMust[T] = this match
    case May(_) => this
    case Must(t) => May(t)

given finiteMayMust[T](using Finite[T]): Finite[MayMust[T]] with {}

given CombineMayMust[T, W <: Widening](using j: Combine[T, W]): Combine[MayMust[T], W] with
  override def apply(v1: MayMust[T], v2: MayMust[T]): MaybeChanged[MayMust[T]] = (v1, v2) match
    case (MayMust.Must(t1), MayMust.Must(t2)) => j(t1, t2).map(MayMust.Must.apply)
    case _ => j(v1.get, v2.get).map(MayMust.May.apply)

  override def lteq(x: MayMust[T], y: MayMust[T]): Boolean = (x,y) match
    case (MayMust.Must(t1), MayMust.Must(t2)) => summon[PartialOrder[T]].lteq(t1,t2)
    case (MayMust.Must(t1), MayMust.May(t2)) => summon[PartialOrder[T]].lteq(t1,t2)
    case (MayMust.May(t1), MayMust.May(t2)) => summon[PartialOrder[T]].lteq(t1, t2)
    case (_, _) => false

given mayMustPO[T](using po: PartialOrder[T]): PartialOrder[MayMust[T]] with
  override def lteq(x: MayMust[T], y: MayMust[T]): Boolean =
    if (!x.isMust && y.isMust)
      false
    else
      po.lteq(x.get, y.get)
