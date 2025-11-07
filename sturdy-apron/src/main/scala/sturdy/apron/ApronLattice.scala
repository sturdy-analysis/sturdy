package sturdy.apron

import apron.*
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
    Profiler.addTime("Abstract1.combine") {
      val result = if(s1.getEnvironment.isEqual(s2.getEnvironment)) {
        if(s2.isIncluded(s2.getCreationManager, s1)) {
          Unchanged(s1)
        } else {
          if(widen) {
            val s2Copy = s2.joinCopy(s2.getCreationManager, s1)
            Changed(s1.widening(s1.getCreationManager, s2Copy))
          } else {
            Changed(s1.joinCopy(s1.getCreationManager, s2))
          }
        }
      } else if(s1.isBottom(s1.getCreationManager)) {
        MaybeChanged(s1, hasChanged = !s2.isBottom(s2.getCreationManager))
      } else if(s2.isBottom(s2.getCreationManager)) {
        Unchanged(s1)
      } else {
        val manager = s1.getCreationManager

        val env1 = s1.getEnvironment
        val env2 = s2.getEnvironment

        val lce = env1.lce(env2)

        val s1ExtEnv = if(lce.isEqual(env1)) s1 else s1.changeEnvironmentCopy(manager, lce, false)
        val s2ExtEnv = if(lce.isEqual(env2)) s2 else s2.changeEnvironmentCopy(manager, lce, false)

        val model1 = getModel(s1)
        val model2 = getModel(s2)

        val env2_minus_env1 = minus(env2, env1).getVars
        val combinable1 =
          if (env2_minus_env1.nonEmpty) {
            s1ExtEnv.assignCopy(
              manager,
              env2_minus_env1,
              env2_minus_env1.map(v => ApronExpr.constant(model2.getCoeff(env2.dimOfVar(v)), null).toIntern(lce)),
              null
            )
          } else
            s1ExtEnv

        val env1_minus_env2 = minus(env1, env2).getVars
        val combinable2 =
          if (env1_minus_env2.nonEmpty)
            s2ExtEnv.assignCopy(
              manager,
              env1_minus_env2,
              env1_minus_env2.map(v => ApronExpr.constant(model1.getCoeff(env1.dimOfVar(v)).sup(), null).toIntern(lce)),
              null
            )
          else
            s2ExtEnv

        val combined =
          if (widen) {
            // This widens recent variables more precisely.
            // For example, [xr = 1] ∇ [xr = 2] = [ 1 <= xr < infty ]
            combinable2.join(manager, combinable1)
            combinable1.widening(manager, combinable2)
          } else {
            combinable1.joinCopy(manager, combinable2)
          }
        
        MaybeChanged(combined, ! (lce.isEqual(env1) && combined.isIncluded(manager, s1ExtEnv)))
      }
      
      result
    }

  def getModel(abs1: Abstract1): apron.Linexpr0 = {
    val manager = abs1.getCreationManager
    manager match {
      case _: apron.Polka => abs1.toGenerator(manager).iterator.flatMap {
        case gen if(gen.getGenerator0Ref.kind == Generator1.VERTEX) => Some(gen.getGenerator0Ref.coord)
        case _ => None
      }.next()
      case _: apron.Octagon =>
        val box = abs1.toBox(manager)
        val gen = abs1.toGenerator(manager)
        val lin = abs1.toLincons(manager)

        abs1.toGenerator(manager).iterator.flatMap {
          case gen if(gen.getGenerator0Ref.kind == Generator1.VERTEX) => Some(gen.getGenerator0Ref.coord)
          case _ => None
        }.next()
      case _: apron.Box => Linexpr0(abs1.toBox(manager).map[Coeff](_.inf()), DoubleScalar(0))
    }
  }

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

given JoinApronBool[Addr: Ordering : ClassTag, Type: Join](using lazyApronState: Lazy[ApronState[Addr, Type]]): Join[ApronBool[Addr, Type]] =
  (e1: ApronBool[Addr,Type], e2: ApronBool[Addr,Type]) =>
    val apronState = lazyApronState.value
    apronState.joinBoolExpr(e1,e2)

given WidenApronBool[Addr: Ordering : ClassTag, Type: Join](using lazyApronState: Lazy[ApronState[Addr, Type]]): Widen[ApronBool[Addr, Type]] =
  (e1: ApronBool[Addr,Type], e2: ApronBool[Addr,Type]) =>
    val apronState = lazyApronState.value
    apronState.widenBoolExpr(e1,e2)