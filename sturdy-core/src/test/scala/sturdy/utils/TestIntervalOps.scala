package sturdy.utils

import org.scalatest.Assertion

trait TestIntervalOps[L, IV]:
  def constant(i: L): IV

  def interval(low: L, high: L): IV

  def shouldContain(n: IV, m: L): Assertion

  def shouldEqual(n: IV, l: L, u: L): Assertion
