package sturdy.values.convert

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.Topped
import sturdy.values.config

import java.nio.ByteOrder

inline def safeTopConversion[A, Config <: ConvertConfig[_]](conf: Config, res: A)(using eff: EffectStack, f: Failure): A =
  if (conf.canFail)
    eff.joinWithFailure(res)(f.fail(ConversionFailure, s"Conversion can fail"))
  else
    res

given ToppedConvert[From, To, VFrom, VTo, Config <: ConvertConfig[_]]
  (using c: Convert[From, To, VFrom, VTo, Config])
  (using EffectStack, Failure)
  : Convert[From, To, Topped[VFrom], Topped[VTo], Config] with

  def apply(from: Topped[VFrom], conf: Config): Topped[VTo] =
    from match
      case Topped.Top => safeTopConversion(conf, Topped.Top)
      case Topped.Actual(v) => Topped.Actual(c(v, conf))

given ToppedConvertSeq[From, To, VFromElem, VTo, Conf <: ConvertConfig[_]]
  (using c: Convert[From, To, Seq[VFromElem], VTo, Conf])
  (using EffectStack, Failure)
  : Convert[From, To, Seq[Topped[VFromElem]], Topped[VTo], Conf] with

  override def apply(from: Seq[Topped[VFromElem]], conf: Conf): Topped[VTo] =
    val elems = from.map {
      case Topped.Top => return safeTopConversion(conf, Topped.Top)
      case Topped.Actual(b) => b
    }
    Topped.Actual(c(elems, conf))


given ToppedConvertToBytes[From, To, VFrom, B, Conf <: ConvertConfig[_]]
  (using c: Convert[From, To, VFrom, Seq[B], config.BytesSize && Conf])
  (using EffectStack, Failure)
  : Convert[From, To, Topped[VFrom], Seq[Topped[B]], config.BytesSize && Conf] with

  override def apply(from: Topped[VFrom], conf: config.BytesSize && Conf): Seq[Topped[B]] = from match
    case Topped.Top =>
      val bytes = Seq.fill(conf._1.bytes)(Topped.Top)
      safeTopConversion(conf, bytes)
    case Topped.Actual(v) => c(v, conf).map(Topped.Actual.apply)

