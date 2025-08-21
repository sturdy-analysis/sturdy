package sturdy.values.addresses

import sturdy.data.{*, given}
import sturdy.values.{Join, Topped}

given ToppedAddressLimits: AddressLimits[Topped[Int], Topped[Int], WithJoin] with
  override def ifAddrLeSize[A: WithJoin](addr: Topped[Int], size: Topped[Int])(f: => A): JOption[WithJoin, A] =
    (addr, size) match
      case (Topped.Actual(a), Topped.Actual(s)) =>
        if(a <= s)
          JOptionA.Some(f)
        else
          JOptionA.None()
      case (Topped.Top, _) | (_, Topped.Top) =>
        given Join[A] = implicitly[WithJoin[A]].j
        implicitly[WithJoin[A]].eff.joinComputations {
          JOptionA.Some(f)
        } {
          JOptionA.None()
        }

  override def ifSizeLeLimit[A: WithJoin](size: Topped[Int], limit: Topped[Int])(ifTrue: => A)(ifFalse: => A): A =
    (size, limit) match
      case (Topped.Actual(s), Topped.Actual(l)) =>
        if (s <= l)
          ifTrue
        else
          ifFalse
      case (Topped.Top, _) | (_, Topped.Top) =>
        given Join[A] = implicitly[WithJoin[A]].j
        implicitly[WithJoin[A]].eff.joinComputations {
          ifTrue
        } {
          ifFalse
        }

given ToppedAddressOffset: AddressOffset[Topped[Int]] with
  override def addOffsetToAddr(offset: Int, addr: Topped[Int]): Topped[Int] =
    addr.map(offset + _)
