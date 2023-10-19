package sturdy.apron

import apron.*

import java.util.Objects


class ApronVar[Context](addrs: Set[sturdy.effect.store.PhysicalAddress[Context]])
  extends sturdy.effect.store.PowPhysicalAddress[Context](addrs), apron.Var:
  def compareTo(v : ApronVar[Context]) : Int = 
    throw new NotImplementedError("ApronVar comparison")
