package sturdy.values.convert

import sturdy.values.Topped
import sturdy.values.config

import java.nio.ByteOrder

given ToppedConvert[From, To, VFrom, VTo, Config](using c: Convert[From, To, VFrom, VTo, Config]): Convert[From, To, Topped[VFrom], Topped[VTo], Config] with
  def apply(from: Topped[VFrom], conf: Config): Topped[VTo] =
    from.map(v => c(v, conf))

given ToppedConvertSeq[From, To, VFromElem, VTo, Conf](using c: Convert[From, To, Seq[VFromElem], VTo, Conf]): Convert[From, To, Seq[Topped[VFromElem]], Topped[VTo], Conf] with
  override def apply(from: Seq[Topped[VFromElem]], conf: Conf): Topped[VTo] =
    val elems = from.map {
      case Topped.Top => return Topped.Top
      case Topped.Actual(b) => b
    }
    Topped.Actual(c(elems, conf))


given ToppedConvertToBytes[From, To, VFrom, B, Conf](using c: Convert[From, To, VFrom, Seq[B], (config.BytesSize, Conf)]): Convert[From, To, Topped[VFrom], Seq[Topped[B]], (config.BytesSize, Conf)] with
  override def apply(from: Topped[VFrom], conf: (config.BytesSize, Conf)): Seq[Topped[B]] = from match
    case Topped.Top => Seq.fill(conf._1.bytes)(Topped.Top)
    case Topped.Actual(v) => c(v, conf).map(Topped.Actual.apply)

