package sturdy.apron

import apron.*

import java.util.Objects
import scala.reflect.ClassTag

case class ApronVar[+Addr](addr: Addr)(using Ordering[Addr], ClassTag[Addr]) extends apron.Var:
  override def clone(): Var = this
  override def compareTo(other: Var): Int = other match
    case otherApronVar: ApronVar[Addr] =>
      Ordering[Addr].compare(this.addr, otherApronVar.addr)
    case _ => -1
  override def toString: String = addr.toString


given ConvertToApronVar[Addr : Ordering : ClassTag]: Conversion[Addr, ApronVar[Addr]] = ApronVar(_)
given ConvertFromApronVar[Addr : Ordering : ClassTag]: Conversion[ApronVar[Addr], Addr] = _.addr

