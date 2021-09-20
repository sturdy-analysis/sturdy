package sturdy.values.convert

import sturdy.effect.failure.Failure
import sturdy.effect.failure.FailureKind

case object ConversionFailure extends FailureKind

/** Provides conversion between values that correspond to types `From` and `To`.
 *
 *  The type parameters `From` and `To` are only used to select instances, whereas
 *  the actual values are represented as `VFrom` and `VTo`.
 */
trait Convert[From, To, VFrom, VTo, Config]:
  def apply(from: VFrom, conf: Config): VTo


object Convert:
  def apply[From, To, V1, V2, Config](from: V1, conf: Config)(using c: Convert[From, To, V1, V2, Config]) = c(from, conf)
