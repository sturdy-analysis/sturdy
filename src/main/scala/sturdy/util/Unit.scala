package sturdy.util

import sturdy.values.JoinValue

given unit: Unit = ()
given JoinValue[Unit] with
  override def joinValues(v1: Unit, v2: Unit): Unit = v1