package sturdy.effect.binarymemory

trait Serialize[V,D,ValTy,DatDecTy,DatEncTy]:
  def decode(dat: D, datDecTy: DatDecTy, valTy: ValTy): V
  def encode(v: V, valTy: ValTy, datEncTy: DatEncTy): D