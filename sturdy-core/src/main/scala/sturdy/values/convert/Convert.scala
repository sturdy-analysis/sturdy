package sturdy.values.convert

import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind

case object ConversionFailure extends FailureKind

/** Provides conversion between values that correspond to types `From` and `To`.
 *
 *  The type parameters `From` and `To` are only used to select instances, whereas
 *  the actual values are represented as `VFrom` and `VTo`.
 */
trait Convert[From, To, VFrom, VTo, Config <: ConvertConfig[_]]:
  def apply(from: VFrom, conf: Config): VTo
  def adaptConfig[Config2 <: ConvertConfig[_]](f: Config2 => Config): Convert[From, To, VFrom, VTo, Config2] =
    (from: VFrom, conf: Config2) => Convert.this.apply(from, f(conf))

trait ConvertConfig[C <: ConvertConfig[C]] { this: C =>
  def canFail: Boolean
  def &&[C2 <: ConvertConfig[_]](c: C2) = new &&[C, C2](this, c)
}
case object NilCC extends ConvertConfig[NilCC.type]:
  override val canFail: Boolean = false
case class SomeCC[T](t: T, canFail: Boolean) extends ConvertConfig[SomeCC[T]]
case class &&[C1 <: ConvertConfig[_], C2 <: ConvertConfig[_]](c1: C1, c2: C2) extends ConvertConfig[&&[C1,C2]]:
  override val canFail: Boolean = c1.canFail || c2.canFail

object Convert:
  def apply[From, To, V1, V2, Config <: ConvertConfig[_]]
    (from: V1, conf: Config)
    (using c: Convert[From, To, V1, V2, Config]): V2 = c(from, conf)

inline def safeConversion[A, Config <: ConvertConfig[_]](conf: Config, res: A)(using eff: EffectStack, f: Failure): A =
  if (conf.canFail)
    eff.joinWithFailure(res)(f.fail(ConversionFailure, s"Conversion can fail"))
  else
    res

trait GaloisConnection[A, B]:
  def asAbstract(a: A): B
  def concretize(b: B)(using Failure): A