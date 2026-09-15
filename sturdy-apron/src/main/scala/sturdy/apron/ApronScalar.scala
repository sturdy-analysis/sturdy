package sturdy.apron

import apron.Scalar

given ScalarOrdering: Ordering[apron.Scalar] with
  override def compare(x: Scalar, y: Scalar): Int =
    x.cmp(y)
