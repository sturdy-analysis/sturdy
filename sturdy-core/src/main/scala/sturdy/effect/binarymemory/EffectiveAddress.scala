package sturdy.effect.binarymemory

trait EffectiveAddress[Base,Offset,Addr]:
  def effectiveAddress(base: Base, offset: Offset): Addr

trait CEffectiveAddress extends EffectiveAddress[Int,Int,Long]:
  override def effectiveAddress(base: Int, offset: Int): Long =
    base.toLong + offset