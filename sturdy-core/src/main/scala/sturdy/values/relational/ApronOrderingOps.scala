package sturdy.values.relational

import sturdy.values.ApronValue

class ApronOrderingOps extends OrderingOps[ApronValue, ApronValue] {

  /** lt(x,y) = 1, if x <= y
   *  lt(x,y) = 0, otherwise
   */
  override def lt(v1: ApronValue, v2: ApronValue): ApronValue = ???

  /** le(x,y) = 1, if x < y
   *  le(x,y) = 0, otherwise
   */
  override def le(v1: ApronValue, v2: ApronValue): ApronValue = ???
}
