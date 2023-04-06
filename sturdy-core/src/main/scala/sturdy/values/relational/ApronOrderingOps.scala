package sturdy.values.relational

import sturdy.values.ApronValue

class ApronOrderingOps extends OrderingOps[ApronValue, ApronValue] {

  /** lt(x,y) = {1 | x < y } ⊔ {0 | not x < y } */
  override def lt(v1: ApronValue, v2: ApronValue): ApronValue = ??? // Share implementation with max


  /** le(x,y) = {1 | x <= y } ⊔ {0 | not x <= y } */
  override def le(v1: ApronValue, v2: ApronValue): ApronValue = ??? // Share implementation with max
}
