package sturdy.values.addresses

import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin}
import sturdy.values.Join

trait AddressOffset[Addr]:
  def addOffsetToAddr(offset: Int, addr: Addr): Addr
  def addOffsetToBaseAddr(offset: Int, addr: Addr): Addr = addOffsetToAddr(offset, addr)
  def moveAddress(addr: Addr, srcOffset: Addr, dstOffset: Addr): Addr

trait AddressLimits[Addr, Size, J[_] <: MayJoin[_]]:
  def addSizeToAddr(size: Size, addr: Addr): Addr
  def ifAddrLeSize[A: J](addr: Addr, size: Size)(f: => A): JOption[J, A]
  def ifSizeLeLimit[A: J](size: Size, limit: Size)(ifTrue: => A)(ifFalse: => A): A

object AddressLimits:
  def apply[Addr, Size, J[_] <: MayJoin[_]](using addrLimit: AddressLimits[Addr, Size, J]): AddressLimits[Addr, Size, J] = addrLimit

given ConcreteAddressLimits: AddressLimits[Int, Int, NoJoin] with
  override def addSizeToAddr(size: Int, addr: Int): Int = addr + size

  override def ifAddrLeSize[A: NoJoin](addr: Int, size: Int)(f: => A): JOption[NoJoin, A] =
    if(addr <= size)
      JOptionC.None()
    else
      JOptionC.Some(f)

  override def ifSizeLeLimit[A: NoJoin](size: Int, limit: Int)(ifTrue: => A)(ifFalse: => A): A =
    if(size <= limit)
      ifTrue
    else
      ifFalse