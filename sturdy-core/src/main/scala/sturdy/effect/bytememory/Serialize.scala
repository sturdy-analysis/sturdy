package sturdy.effect.bytememory

trait Serialize[V,D,EncInfo,DecInfo]:
  def decode(dat: D, decInfo: DecInfo): V
  def encode(v: V, encInfo: EncInfo): D
