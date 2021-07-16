package sturdy.values.references

trait ReferenceOps[Addr, V] {
  def nullValue: V
  def refValue(addr: Addr): V
  def refAddr(v: V): Addr
}
