package sturdy.values.relational

import sturdy.values.ApronValue

class ApronEqOps extends EqOps[ApronValue, ApronValue] {

  /** equ(x,y) = { 1 | x = y } ⊔ { 0 | x != y } */
  override def equ(v1: ApronValue, v2: ApronValue): ApronValue = ??? // Share implementation with max

  /** neq(x,y) = { 0 | x = y } ⊔ { 1 | x != y } */
  override def neq(v1: ApronValue, v2: ApronValue): ApronValue = ??? // Share implementation with max
}
