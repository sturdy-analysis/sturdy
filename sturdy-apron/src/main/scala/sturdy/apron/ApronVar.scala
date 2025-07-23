package sturdy.apron

import apron.*

import java.util.Objects
import scala.reflect.ClassTag

case class ApronVar[Addr](addr: Addr)(using Ordering[Addr], ClassTag[Addr]) extends apron.Var:
  override def clone(): Var = this
  override def compareTo(other: Var): Int = other match
    case otherApronVar: ApronVar[Addr @unchecked] =>
      Ordering[Addr].compare(this.addr, otherApronVar.addr)
    case _ => -1
  override def toString: String = addr.toString
  def mapAddr(f: Addr => Addr): ApronVar[Addr] = ApronVar(f(addr))

object ApronVar:
  def unapply[Addr](addr: ApronVar[Addr]): Option[Addr] = Some(addr.addr)

given ConvertToApronVar[Addr : Ordering : ClassTag]: Conversion[Addr, ApronVar[Addr]] = ApronVar(_)
given ConvertFromApronVar[Addr : Ordering : ClassTag]: Conversion[ApronVar[Addr], Addr] = _.addr

