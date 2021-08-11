package sturdy.values.conversion

import java.lang.{Double => JDouble, Float => JFloat}

trait ConvertFloatDoubleOps[F, D]:
  def doubleToFloat(d: D): F
  def floatToDouble(f: F): D

given concreteConvertFloatDoubleOps: ConvertFloatDoubleOps[Float, Double] with
  def doubleToFloat(d: Double): Float =
    if (!d.isNaN) {
      d.toFloat
    } else {
      val nan64bits = JDouble.doubleToRawLongBits(d)
      val signField = (nan64bits >>> 63) << 31
      val significandField = (nan64bits << 12) >>> 41
      val fields = signField | significandField
      val nan32bits = 0x7fc00000 | fields.toInt
      JFloat.intBitsToFloat(nan32bits)
    }

  def floatToDouble(f: Float): Double =
    if (!f.isNaN) {
      f.toDouble
    } else {
      val nan32bits = JFloat.floatToRawIntBits(f) & 0X00000000FFFFFFFFL
      val signField = (nan32bits >>> 31) << 63
      val significandField = (nan32bits << 41) >>> 12
      val fields = signField | significandField
      val nan64bits = 0X7FF8000000000000L | fields
      JDouble.longBitsToDouble(nan64bits)
    }