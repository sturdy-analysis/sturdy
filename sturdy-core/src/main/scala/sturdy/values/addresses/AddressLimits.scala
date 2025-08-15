package sturdy.values.addresses

import sturdy.data.{JOption, JOptionC, MayJoin, NoJoin}
import sturdy.values.Join

trait AddressLimits[Addr, Size, J[_] <: MayJoin[_]]:
  def ifAddrLeSize[A: J](addr: Addr, size: Size)(f: => A): JOption[J, A]
  def ifSizeLeLimit[A: J](size: Size, limit: Size)(ifTrue: => A)(ifFalse: => A): A

given ConcreteAddressLimits: AddressLimits[Int, Int, NoJoin] with
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