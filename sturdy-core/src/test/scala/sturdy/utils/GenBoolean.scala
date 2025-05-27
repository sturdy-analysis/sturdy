package sturdy.utils

import org.scalacheck.Gen.Choose
import org.scalacheck.{Gen, Shrink}
import sturdy.values.Topped

import scala.math.Integral.Implicits.infixIntegralOps
import scala.math.Ordering.Implicits.infixOrderingOps

object GenBoolean:

  case class BooleanVal(con: Boolean, abs: Topped[Boolean])

  def genBoolean: Gen[BooleanVal] =
    for {
      abs <- Gen.oneOf(Topped.Actual(true),Topped.Actual(false),Topped.Top);
      con <- abs match
               case Topped.Actual(true) => Gen.const(true)
               case Topped.Actual(false) => Gen.const(false)
               case Topped.Top => Gen.oneOf(true,false)
    } yield(BooleanVal(con, abs))