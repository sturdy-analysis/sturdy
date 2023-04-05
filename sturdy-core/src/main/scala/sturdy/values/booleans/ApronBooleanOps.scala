package sturdy.values.booleans

import sturdy.values.ApronValue

class ApronBooleanOps extends BooleanOps[ApronValue] {
  /** Represent true as 0 and false as 1 */
  override def boolLit(b: Boolean): ApronValue = ???

  /** not(x) = abs(x-1) */
  override def not(v: ApronValue): ApronValue = ???

  /** and(x,y) = x*y */
  override def and(v1: ApronValue, v2: ApronValue): ApronValue = ???

  /** or(x,y) = max(x,y) */
  override def or(v1: ApronValue, v2: ApronValue): ApronValue = ???
}
