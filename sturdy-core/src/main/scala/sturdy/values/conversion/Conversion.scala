package sturdy.values.conversion

import sturdy.effect.failure.{Failure, FailureKind}

/*
 * Most conversion rules in this package have been copied from:
 *   https://github.com/satabin/swam/tree/fd76cb96759fb7bbd84e476d0b2a9fd1e47b9c08/runtime/src/swam/runtime
 */

case object ConversionFailure extends FailureKind


trait Convert[T1, T2, Config]:
  def apply(t1: T1, conf: Config): T2

enum OverflowConfig:
  case Allow
  case Fail
  case JumpToBounds
case class SignedConfig(signed: Boolean)

given concreteConvertIntDouble(using f: Failure): Convert[Int, Double, SignedConfig] with
  override def apply(i: Int, conf: SignedConfig): Double = conf match
    case SignedConfig(true) => i.toDouble
    case SignedConfig(false) => (i & 0X00000000FFFFFFFFL).toDouble

given concreteConvertDoubleInt(using f: Failure): Convert[Double, Int, (OverflowConfig, SignedConfig)] with
  def apply(d: Double, conf: (OverflowConfig, SignedConfig)) = conf match
    case (OverflowConfig.Allow, SignedConfig(true)) => d.toInt
    case (OverflowConfig.Allow, SignedConfig(false)) => d.toLong.toInt
    case (OverflowConfig.Fail, SignedConfig(true)) =>
      if (d.isNaN)
        f.fail(ConversionFailure, s"double $d cannot be converted")
      else if (d >= -Int.MinValue.toDouble || d <= Int.MinValue.toDouble - 1)
        f.fail(ConversionFailure, s"double $d out of integer range")
      else
        d.toInt
    case (OverflowConfig.Fail, SignedConfig(false)) =>
      if (d.isNaN)
        f.fail(ConversionFailure, s"double $d cannot be converted")
      else if (d >= -Int.MinValue.toDouble * 2.0d || d <= -1.0)
        f.fail(ConversionFailure, s"double $d out of integer range")
      else
        d.toLong.toInt
    case (OverflowConfig.JumpToBounds, SignedConfig(true)) =>
      if (d.isNaN)
        0
      else if (d >= -Int.MinValue.toDouble)
        Int.MaxValue
      else if (d < Int.MinValue)
        Int.MinValue
      else
        d.toInt
    case (OverflowConfig.JumpToBounds, SignedConfig(false)) =>
      if (d.isNaN)
        0
      else if (d >= -Int.MinValue.toDouble * 2.0d)
        -1
      else if (d < 0.0)
        0
      else
        d.toLong.toInt

