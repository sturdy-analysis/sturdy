package sturdy.util

import org.scalacheck.Gen.Choose
import org.scalacheck.{Gen, Shrink}
import sturdy.values.floating.FloatSpecials

import scala.collection.mutable.ArrayBuffer

object GenFloatSpecials:
  def genFloatSpecials[N: Numeric: Choose]: Gen[FloatSpecials] =
    for {
      negInfinity <- Gen.oneOf(true, false)
      posInfinity <- Gen.oneOf(true, false)
      nan <- Gen.oneOf(true, false)
    }
    yield FloatSpecials(negInfinity = negInfinity, posInfinity = posInfinity, nan = nan)

  def shrinkFloatSpecials: Shrink[FloatSpecials] = Shrink(
    specials =>
      val stream: ArrayBuffer[FloatSpecials] = ArrayBuffer.empty
      if(specials.negInfinity)
        stream += specials.setNegInfinity(false)
      if(specials.posInfinity)
        stream += specials.setPosInfinity(false)
      if(specials.nan)
        stream += specials.setNaN(false)
      stream.toStream
  )