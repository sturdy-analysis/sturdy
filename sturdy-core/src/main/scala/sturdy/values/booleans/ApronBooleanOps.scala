package sturdy.values.booleans

import apron.Manager
import sturdy.values.ApronValue
import sturdy.values.integer.ApronIntegerOps

final class ApronBooleanOps(using manager: Manager) extends BooleanOps[ApronValue] {
  /** Represent true as 0 and false as 1 */
  override def boolLit(b: Boolean): ApronValue = ???

  /** not(x) = neg(x-1) */
  override def not(v: ApronValue): ApronValue = ???

  /** and(x,y) = x*y */
  override def and(v1: ApronValue, v2: ApronValue): ApronValue = ApronIntegerOps().mul(v1,v2)

  /** or(x,y) = max(x,y) */
  override def or(v1: ApronValue, v2: ApronValue): ApronValue = ApronIntegerOps().max(v1,v2)
}
