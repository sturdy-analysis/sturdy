package sturdy.values.floating

import sturdy.values.ApronValue

final class ApronFloatOps[B](using Fractional[B]) extends FloatOps[B, ApronValue] {
  override def floatingLit(f: B): ApronValue = ???

  override def randomFloat(): ApronValue = ???

  override def add(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def sub(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def mul(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def div(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def min(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def max(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def absolute(v: ApronValue): ApronValue = ???

  override def negated(v: ApronValue): ApronValue = ???

  override def sqrt(v: ApronValue): ApronValue = ???

  override def ceil(v: ApronValue): ApronValue = ???

  override def floor(v: ApronValue): ApronValue = ???

  override def truncate(v: ApronValue): ApronValue = ???

  override def nearest(v: ApronValue): ApronValue = ???

  override def copysign(v: ApronValue, sign: ApronValue): ApronValue = ???
}
