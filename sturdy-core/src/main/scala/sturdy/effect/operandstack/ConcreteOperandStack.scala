package sturdy.effect.operandstack

trait ConcreteOperandStack[V] extends GenericOperandStack[V]:
  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    val snapshot = stack
    super.joinComputations(ensureUnchanged(f, snapshot))(ensureUnchanged(g, snapshot))

  private def ensureUnchanged[A](f: => A, oldStack: List[V]): A =
    val result = f
    if (!(oldStack eq this.stack))
      throw new IllegalStateException(s"Concrete operand stack has changed at join point, which is illegal. Old stack was $oldStack, new stack is ${this.stack}.")
    result
    
  def getStack: List[V] = stack