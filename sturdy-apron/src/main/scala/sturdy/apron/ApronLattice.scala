package sturdy.apron

import apron.{Abstract1, Coeff, Environment, Interval, Manager, StringVar}
import sturdy.util.{Lazy, Profiler}
import sturdy.values.{Combine, Top, Widen, Widening}

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

    val env2_minus_env1 = minus(env2,env1).getVars
    val combinable1 =
      if(env2_minus_env1.nonEmpty)
        s1ExtEnv.assignCopy(
          manager,
          env2_minus_env1,
          env2_minus_env1.map(v => ApronExpr.constant(s2ExtEnv.getBound(manager, v), null).toIntern(lce)),
          null
        )
      else
        s1ExtEnv

    val env1_minus_env2 = minus(env1,env2).getVars
    val combinable2 =
      if(env1_minus_env2.nonEmpty)
        s2ExtEnv.assignCopy(
          manager,
          env1_minus_env2,
          env1_minus_env2.map(v => ApronExpr.constant(s1ExtEnv.getBound(manager, v), null).toIntern(lce)),
          null
        )
      else
        s2ExtEnv

    val combined =
      if (widen)
        combinable1.widening(manager, combinable2)
      else
        combinable1.joinCopy(manager, combinable2)

    val res = MaybeChanged(combined, ! (lce.isEqual(env1) && combined.isIncluded(manager, s1ExtEnv)))
    
    res

  def minus[A](env1: Environment, env2: Environment): Environment =
    var env = env1
    for (x <- env1.getVars)
      if (env2.hasVar(x))
        env = env.remove(Array(x))
    env


given JoinApronExpr[Addr: Ordering : ClassTag, Type: Join](using lazyApronState: Lazy[ApronState[Addr, Type]]): Join[ApronExpr[Addr, Type]] =
  (e1: ApronExpr[Addr,Type], e2: ApronExpr[Addr,Type]) =>
    val apronState = lazyApronState.value
    apronState.join(e1,e2)

given WidenApronExpr[Addr: Ordering : ClassTag, Type: Join](using lazyApronState: Lazy[ApronState[Addr, Type]]): Widen[ApronExpr[Addr, Type]] =
  (e1: ApronExpr[Addr,Type], e2: ApronExpr[Addr,Type]) =>
    val apronState = lazyApronState.value
    apronState.widen(e1,e2)
