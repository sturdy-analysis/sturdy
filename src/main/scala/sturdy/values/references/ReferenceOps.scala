package sturdy.values.references

import sturdy.effect.failure.{Failure, FailureKind}

case object NullDereference extends FailureKind

trait ReferenceOps[Addr, V] {
  def nullValue: V
  def refValue(addr: Addr): V
  def refAddr(v: V): Addr
}

given ConcreteReferenceOps[Addr](using f: Failure): ReferenceOps[Addr, Option[Addr]] with
  def nullValue: Option[Addr] = None
  def refValue(addr: Addr): Option[Addr] = Some(addr)
  def refAddr(v: Option[Addr]): Addr = v.getOrElse(f.fail(NullDereference, ""))
