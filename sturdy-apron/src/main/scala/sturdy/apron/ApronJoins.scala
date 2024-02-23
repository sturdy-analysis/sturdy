package sturdy.apron

import apron.{Abstract1, Coeff, Environment, Manager, StringVar}
import sturdy.values.{Combine, Widen, Widening}

import scala.reflect.ClassTag
// import sturdy.apron.Apron.debugJoinWiden
import sturdy.data.{given}
import sturdy.effect.failure.Failure
import sturdy.values.{Changed, Join, MaybeChanged, Unchanged}

given Abstract1Join: Join[Abstract1] with
  override def apply(v1: Abstract1, v2: Abstract1): MaybeChanged[Abstract1] =
    ApronJoins.combineAbstract1(v1, v2, false)

given Abstract1Widen: Widen[Abstract1] with
  override def apply(v1: Abstract1, v2: Abstract1): MaybeChanged[Abstract1] =
    ApronJoins.combineAbstract1(v1, v2, true)

object ApronJoins:
  def combineAbstract1(s1: Abstract1, s2: Abstract1, widen: Boolean): MaybeChanged[Abstract1] =
    val manager = s1.getCreationManager

    val env1 = s1.getEnvironment
    val env2 = s2.getEnvironment
    val lce = env1.lce(env2)

    val s1ExtEnv = s1.changeEnvironmentCopy(manager, lce, false)
    val s2ExtEnv = s2.changeEnvironmentCopy(manager, lce, false)

    val top = ApronExpr.topConstant(null).toIntern(lce)

    val env2_minus_env1 = minus(env2,env1).getVars
    val combinable1 =
      if(env2_minus_env1.nonEmpty)
        s1ExtEnv.assignCopy(
          manager,
          env2_minus_env1,
          Array.fill(env2_minus_env1.length)(top),
          s2ExtEnv
        )
      else
        s1ExtEnv

    val env1_minus_env2 = minus(env1,env2).getVars
    val combinable2 =
      if(env1_minus_env2.nonEmpty)
        s2ExtEnv.assignCopy(
          manager,
          env1_minus_env2,
          Array.fill(env1_minus_env2.length)(top),
          s1ExtEnv
        )
      else
        s2ExtEnv

    val combined =
      if (widen)
        combinable1.widening(manager, combinable2)
      else
        combinable1.joinCopy(manager, combinable2)

    val res = MaybeChanged(combined, ! (lce.isEqual(env1) && combined.isIncluded(manager, s1ExtEnv)))

    println(s"Abstract1 Join:\ns1 = $s1\thashcode = ${s1.hashCode(manager)}\ns2 = $s2\thashcode = ${s2.hashCode(manager)}\ns1 ${if(widen) "∇" else "⊔"} s2 = ${res}\thashcode = ${res.get.hashCode()}\n\n")

    res

  def minus[A](env1: Environment, env2: Environment): Environment =
    var env = env1
    for (x <- env1.getVars)
      if (env2.hasVar(x))
        env = env.remove(Array(x))
    env

given CombineApronExpr[Addr: Ordering: ClassTag, Type : Join, W <: Widening](using apronState: ApronState[Addr,Type]): Combine[ApronExpr[Addr,Type], W] =
  (e1: ApronExpr[Addr,Type], e2: ApronExpr[Addr,Type]) =>
    val resultType = Join(e1._type, e2._type).get
    val iv1 = apronState.getBound(e1)
    val iv2 = apronState.getBound(e2)
    apronState.withTempVars(resultType) {
      case (result, List()) =>
        apronState.join {
          apronState.assign(result, e1)
        } {
          apronState.assign(result, e2)
        }
        val iv3 = apronState.getBound(e2)
        MaybeChanged(ApronExpr.addr(result, resultType), iv3.isLeq(iv1))
    }

given Join[Coeff] = (c1,c2) =>
  val inf1 = c1.inf()
  val inf2 = c2.inf()
  val newInf =
    if(inf1.cmp(inf2) <= 0)
      inf1
    else
      inf2

  val sup1 = c1.sup()
  val sup2 = c2.sup()
  val newSup =
    if(sup1.cmp(sup2) <= 0)
      sup2
    else
      sup1

  val upperBound = apron.Interval(newInf, newSup)

  MaybeChanged(upperBound, ! (upperBound.isEqual(c1) || upperBound.isEqual(c2)))