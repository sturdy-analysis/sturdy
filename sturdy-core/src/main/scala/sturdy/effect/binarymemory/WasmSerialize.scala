package sturdy.effect.binarymemory

trait WasmSerialize[V,D,EncInfo,DecInfo]:
  def decode(dat: D, decInfo: DecInfo): V
  def encode(v: V, encInfo: EncInfo): D
