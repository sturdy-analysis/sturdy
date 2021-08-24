package sturdy.effect.binarymemory

trait EffectiveAddress[Base,Offset,Addr]:
  def effectiveAddress(base: Base, offset: Offset): Addr