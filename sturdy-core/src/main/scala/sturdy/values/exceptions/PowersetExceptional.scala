package sturdy.values.exceptions

import sturdy.effect.{Join, Effectful, given}
import sturdy.values.Powerset

given PowersetExceptional[E]: Exceptional[E, Powerset[E], Join] with
  override def exception(exc: E): Powerset[E] = Powerset(exc)
  override def handle[A](es: Powerset[E])(f: E => A): Join[A] ?=> A =
    summon[Effectful].joinComputationsIterable(es.set.map(e => () => f(e)))
