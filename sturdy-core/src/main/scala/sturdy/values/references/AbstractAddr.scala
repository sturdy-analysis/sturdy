package sturdy.values.references

import sturdy.values.ordering.EqOps
import sturdy.values.{Finite, Join, MaybeChanged, PartialOrder, Powerset, Structural, Topped}

trait AbstractAddr[+Addr]:
  def isEmpty: Boolean
  def isStrong: Boolean
  def reduce[A](f: Addr => A)(using Join[A]): A
  def iterator: Iterator[Addr]

given abstractAddrEqOps[AA <: AbstractAddr[_]]: EqOps[AA, Topped[Boolean]] with
  override def equ(v1: AA, v2: AA): Topped[Boolean] =
    if (v1.isEmpty && v2.isEmpty)
      Topped.Actual(true)
    else if (v1.isStrong && v2.isStrong)
      Topped.Actual(v1 == v2)
    else
      Topped.Top
  override def neq(v1: AA, v2: AA): Topped[Boolean] = equ(v1, v2).map(!_)

case class PowersetAddr[A, AA <: AbstractAddr[A]](addrs: Set[AA]) extends AbstractAddr[AA]:
  override def isEmpty: Boolean = addrs.forall(_.isEmpty)
  override def isStrong: Boolean = addrs.size == 1 && addrs.head.isStrong
  override def reduce[B](f: AA => B)(using Join[B]): B = addrs.map(f).reduce { case (a1, a2) =>
    Join(a1, a2).get
  }
  override def iterator: Iterator[AA] = addrs.iterator
  def +(other: PowersetAddr[A, AA]): PowersetAddr[A, AA] = PowersetAddr(addrs.union(other.addrs))

object PowersetAddr:
  def apply[A, AA <: AbstractAddr[A]](addr: AA): PowersetAddr[A, AA] = PowersetAddr(Set(addr))
  def apply[A, AA <: AbstractAddr[A]](addr: AA, addrs: AA*): PowersetAddr[A, AA] = PowersetAddr(Set(addr) ++ addrs)

given joinPowersetAddr[A, AA <: AbstractAddr[A]]: Join[PowersetAddr[A, AA]] with
  override def apply(v1: PowersetAddr[A, AA], v2: PowersetAddr[A, AA]): MaybeChanged[PowersetAddr[A, AA]] =
    val joinedSet = v1.addrs ++ v2.addrs
    MaybeChanged(new PowersetAddr(joinedSet), joinedSet.size > v1.addrs.size)

given finitePowersetAddr[A, AA <: AbstractAddr[A]](using Finite[AA]): Finite[PowersetAddr[A, AA]] with {}

given powersetAddrPO[A, AA <: AbstractAddr[A]](using Structural[AA]): PartialOrder[PowersetAddr[A, AA]] with
  override def lteq(x: PowersetAddr[A, AA], y: PowersetAddr[A, AA]): Boolean = x.addrs.subsetOf(y.addrs)
