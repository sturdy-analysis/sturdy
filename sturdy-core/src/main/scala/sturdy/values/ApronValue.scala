package sturdy.values

import apron.{Abstract1, DoubleScalar, Environment, Interval, Manager, MpqScalar, Scalar, Texpr1CstNode, Texpr1Intern, Texpr1Node, Texpr1VarNode, Var}

final class ApronValue(val domain: Abstract1,
                       val expr: Texpr1Node):
  def getBound: apron.Interval =
    domain.getBound(domain.getCreationManager, Texpr1Intern(domain.getEnvironment, expr))

  /** Assign expression contained in the apron value to the given variable. Creates a copy of the domain. */
  def assign(x: Var): ApronValue =
    val env = domain.getEnvironment.add(Array(x), Array.empty[Var])
    val dom = domain.changeEnvironmentCopy(domain.getCreationManager, env, false)
    dom.assign(dom.getCreationManager, x, Texpr1Intern(dom.getEnvironment, expr), null)
    ApronValue(dom, Texpr1VarNode(x))

  def join(other: ApronValue): ApronValue =
    val t: Var = TempVar()
    joinWith(t,other)

  def joinWith(t: Var, other: ApronValue): ApronValue =
    val v1 = this.assign(t)
    val v2 = other.assign(t)
    v1.domain.join(v1.domain.getCreationManager, v2.domain)
    v1

  def meet(other: ApronValue): ApronValue =
    val t: Var = TempVar()
    meetWith(t, other)

  def meetWith(t: Var, other: ApronValue): ApronValue =
    val v1 = this.assign(t)
    val v2 = other.assign(t)
    v1.domain.meet(v1.domain.getCreationManager, v2.domain)
    v1

object ApronValue {
  inline def scalar(double: Double): Scalar = DoubleScalar(double)

  inline def interval(lower: Double, upper: Double)(using manager: Manager): ApronValue =
    interval(scalar(lower), scalar(upper))

  def interval(lower: Scalar, upper: Scalar)(using manager: Manager): ApronValue =
    val env = Environment()
    ApronValue(domain = new Abstract1(manager, env),
      expr = Texpr1CstNode(Interval(lower, upper)))

  val negInfinity: Scalar =
    val s = new DoubleScalar()
    s.setInfty(-1)
    s
  val posInfinity: Scalar =
    val s = new DoubleScalar()
    s.setInfty(-1)
    s

  extension(x: Scalar)
    def toDouble(): Double =
      val res = Array[Double](0)
      x.toDouble(res,0)
      res(0)
}

given JoinApronValue: Join[ApronValue] with
  override def apply(v1: ApronValue, v2: ApronValue): MaybeChanged[ApronValue] =
    val t = TempVar()
    val d1 = v1.assign(t).domain
    val d2 = v2.assign(t).domain

    val dJoin = d1.joinCopy(d1.getCreationManager, d2)

    MaybeChanged(ApronValue(dJoin, Texpr1VarNode(t)),
      // TODO: This check is very expensive. Maybe there is a more performant way to check if they are different.
      !(dJoin.isIncluded(dJoin.getCreationManager, d1) && dJoin.isIncluded(dJoin.getCreationManager, d2))
    )

given WidenApronValue: Widen[ApronValue] with
  override def apply(v1: ApronValue, v2: ApronValue): MaybeChanged[ApronValue] =
    val t = TempVar()
    val d1 = v1.assign(t).domain
    val d2 = v2.assign(t).domain

    val dJoin = d1.widening(d1.getCreationManager, d2)

    MaybeChanged(ApronValue(dJoin, Texpr1VarNode(t)),
      // TODO: This check is very expensive. Maybe there is a more performant way to check if they are different.
      !(dJoin.isIncluded(dJoin.getCreationManager, d1) && dJoin.isIncluded(dJoin.getCreationManager, d2))
    )

final case class TempVar(x: Int) extends Var:
  override def compareTo(other: Var): Int =
    other match
      case TempVar(y) if x == y => 0
      case _ => -1

  override def clone(): Var = TempVar(x)

object TempVar:
  private var latestVar = 0
  def apply(): TempVar =
    val v = TempVar(latestVar)
    latestVar += 1
    v