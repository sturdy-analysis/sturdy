package sturdy.data

import sturdy.values.{Finite, Widening, Combine}


given finiteUnit: Finite[Unit] with {}

given unit: Unit = ()
given CombineUnit[W <: Widening]: Combine[Unit, W] with
  override def apply(v1: Unit, v2: Unit): Unit = v1
