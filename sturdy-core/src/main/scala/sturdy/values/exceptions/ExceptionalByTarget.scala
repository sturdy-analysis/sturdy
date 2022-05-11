package sturdy.values.exceptions

import sturdy.data.{WithJoin, mapJoin}
import sturdy.values.{Combine, Join, MaybeChanged, Widen}

class ExceptionalByTarget[Exc, Trg, V](byTarget: Exc => (Trg, V), asExc: (Trg, V) => Exc)
  extends Exceptional[Exc, Map[Trg, V], WithJoin]:

  override def exception(exc: Exc): Map[Trg, V] = Map(byTarget(exc))
  override def handle[A](e: Map[Trg, V])(f: Exc => A): WithJoin[A] ?=> A =
    mapJoin[Exc, A](e.map(asExc.tupled), f)
