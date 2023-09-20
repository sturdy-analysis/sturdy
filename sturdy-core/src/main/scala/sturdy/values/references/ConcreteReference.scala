package sturdy.values.references

import sturdy.effect.failure.Failure
import sturdy.values.{Finite, Structural}

enum Reference[+Addr]:
  case Null
  case Addr(a: Addr, managed: Boolean)

  def getOrElse[A >: Addr](default: => A): A = this match
    case Null => default
    case Addr(a, _) => a

  def isManaged: Boolean = this match
    case Null => true
    case Addr(_, m) => m

given structuralReference[A](using Structural[A]): Structural[Reference[A]] with {}
given finiteReference[A](using Finite[A]): Finite[Reference[A]] with {}

given referenceOps[Addr] (using f: Failure): ReferenceOps[Addr, Reference[Addr]] with
  def nullValue: Reference[Addr] = Reference.Null
  def refValue(addr: Addr): Reference[Addr] = Reference.Addr(addr, true)
  def unmanagedRefValue(addr: Addr): Reference[Addr] = Reference.Addr(addr, false)
  def refAddr(v: Reference[Addr]): Addr = v.getOrElse(f.fail(NullDereference, ""))
