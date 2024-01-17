package sturdy.language.tip.backward.values


trait BackEqOps[V, B]:
  def equ(v1: V => V, v2: V => V, r: B): B
  def neq(v1: V => V, v2: V => V, r: B): B
