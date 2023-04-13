package sturdy.data

import sturdy.values.{Finite, Widening, Combine, MaybeChanged, Unchanged}


given finiteUnit: Finite[Unit] with {}

given unit: Unit = ()
given CombineUnit[W <: Widening]: Combine[Unit, W] with
  override def apply(v1: Unit, v2: Unit): MaybeChanged[Unit] = Unchanged(v1)

  override def lteq(x: Unit, y: Unit): Boolean = true
