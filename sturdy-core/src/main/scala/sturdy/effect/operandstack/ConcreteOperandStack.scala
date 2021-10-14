package sturdy.effect.operandstack

trait ConcreteOperandStack[V] extends GenericOperandStack[V]:
  def getStack: List[V] = stack