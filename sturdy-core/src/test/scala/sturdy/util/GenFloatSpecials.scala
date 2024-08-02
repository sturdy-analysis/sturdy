package sturdy.util

import java.lang.{Double => JDouble}

import org.scalacheck.Gen.Choose
import org.scalacheck.{Gen, Shrink}

import scala.collection.mutable.ArrayBuffer

import sturdy.values.floating.FloatSpecials

object GenFloatSpecials:
  def genFloatSpecials(d: Double): Gen[FloatSpecials] =
    for {
      negInfinity <- if(d.isNegInfinity) Gen.const(true) else Gen.oneOf(true, false)
      negZero <- if(JDouble.doubleToRawLongBits(d) == JDouble.doubleToRawLongBits(-0.0d)) Gen.const(true) else Gen.oneOf(true, false)
      posInfinity <- if(d.isPosInfinity) Gen.const(true) else Gen.oneOf(true, false)
      nan <- if(d.isNaN) Gen.const(true) else Gen.oneOf(true, false)
    }
    yield FloatSpecials(negInfinity = negInfinity, negZero = negZero, posInfinity = posInfinity, nan = nan)

  given shrinkFloatSpecials: Shrink[FloatSpecials] = Shrink(
    specials =>
      val stream: ArrayBuffer[FloatSpecials] = ArrayBuffer.empty
      if(specials.negInfinity)
        stream += specials.setNegInfinity(false)
      if (specials.negZero)
        stream += specials.setNegZero(false)
      if(specials.posInfinity)
        stream += specials.setPosInfinity(false)
      if(specials.nan)
        stream += specials.setNaN(false)
      stream.toStream
  )