package sturdy.values.exceptions

import sturdy.effect.NoJoin

given ConcreteExceptional[E]: Exceptional[E, E, NoJoin] with
  override def exception(exc: E): E = exc
  override def handle[A](e: E)(f: E => A): NoJoin[A] ?=> A = f(e)
