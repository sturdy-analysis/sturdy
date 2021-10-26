package sturdy.util

class Exact[T](val t: T):
  override def equals(obj: Any): Boolean = obj match
    case o: Exact[_] => o.t.asInstanceOf[AnyRef] eq this.t.asInstanceOf[AnyRef]
    case _ => false
  override def hashCode(): Int = t.hashCode()