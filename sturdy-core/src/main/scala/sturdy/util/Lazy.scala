package sturdy.util

class Lazy[T](arg: => T):
  lazy val value = arg

def lazily[T](t: => T): Lazy[T] = new Lazy(t)
def fromEager[T](using t: T): Lazy[T] = new Lazy(t)

given force[T](using t: Lazy[T]): T = t.value
