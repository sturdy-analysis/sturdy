package sturdy.values.relational

import sturdy.values.ApronValue

class ApronEqOps extends EqOps[ApronValue, ApronValue] {

  /** equ(x,y) = 1, if x = y
   *  equ(x,y) = 0, otherwise
   */
  override def equ(v1: ApronValue, v2: ApronValue): ApronValue = ???

  /** neq(x,y) = 1, if x != y
   *  neq(x,y) = 0, otherwise
   */
  override def neq(v1: ApronValue, v2: ApronValue): ApronValue = ???
}
