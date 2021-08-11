package sturdy.values.conversion

trait ConvertFloatDoubleOps[F, D]:
  def doubleToFloat(d: D): F
  
  def floatToDouble(f: F): D
