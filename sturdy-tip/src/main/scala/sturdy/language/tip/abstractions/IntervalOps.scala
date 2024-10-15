package sturdy.language.tip.abstractions

import sturdy.data.*
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.language.tip
import sturdy.language.tip.{TipElaborationOps, Exp, TipGradualOps}
import sturdy.values.integer.*
import sturdy.values.{PartialOrder, Top}

import scala.math.Numeric.Implicits.given
import scala.math.Ordering.Implicits.given

given NumericIntervalElaborationOps: TipElaborationOps[NumericInterval[Int]] with
  def abstractToExpr(v: NumericInterval[Int]): Exp =
    Exp.Record(Seq(("low", Exp.NumLit(v.low)), ("high", Exp.NumLit(v.high))))

  val abstractionFunction: tip.Function =
    parse(
      """alpha(x) {
        |  return {low: x, high: x};
        |}""".stripMargin)

  val precisionFunction: tip.Function =
    parse(
      """prec(x, y) {
         |  return (y.low <= x.low + x.high <= y.high) > 1;
         |}""".stripMargin)
