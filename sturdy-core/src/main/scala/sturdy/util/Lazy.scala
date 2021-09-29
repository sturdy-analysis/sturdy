package sturdy.util

class Lazy[T](arg: => T):
  lazy val value = arg

def lazily[T](t: => T): Lazy[T] = new Lazy(t)
def force[T](using t: Lazy[T]): T = t.value
