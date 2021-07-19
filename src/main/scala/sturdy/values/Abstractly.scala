package sturdy.values

trait Abstractly[C, A]:
  def abstractly(c: C): A

given concreteAbstractly[C]: Abstractly[C, C] with
  def abstractly(c: C): C = c
