package sturdy.values.exceptions

import sturdy.data.*
import sturdy.effect.Effectful
import sturdy.values.Powerset

given PowersetExceptional[E]: Exceptional[E, Powerset[E], WithJoin] with
  override def exception(exc: E): Powerset[E] = Powerset(exc)
  override def handle[A](es: Powerset[E])(f: E => A): WithJoin[A] ?=> A =
    mapJoin(es.set, f)
