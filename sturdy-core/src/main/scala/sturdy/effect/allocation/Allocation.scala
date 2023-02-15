package sturdy.effect.allocation

import sturdy.effect.Effect

trait Allocation[Addr, -Site] extends Effect:
  def apply(site: Site): Addr
