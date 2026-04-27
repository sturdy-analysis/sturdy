package sturdy.apron

import apron.*
import gmp.Mpfr
import org.apache.commons.math3.exception.MathIllegalStateException
import org.apache.commons.math3.optim.linear.{LinearConstraint, LinearConstraintSet, LinearObjectiveFunction, Relationship, SimplexSolver}
import org.apache.commons.math3.optim.MaxIter
import sturdy.util.{Lazy, Profiler}
import sturdy.values.{Combine, Top, Topped, Widen, Widening}

import scala.collection.immutable.ArraySeq
import scala.collection.mutable
import scala.reflect.ClassTag
// import sturdy.apron.Apron.debugJoinWiden
import sturdy.data.{given}
import sturdy.effect.failure.Failure
import sturdy.values.{Changed, Join, MaybeChanged, Unchanged}
import scala.jdk.CollectionConverters.*

given Abstract1Join(using manager: Manager): Join[(Abstract1,Set[Lincons1])] with
  override def apply(v1: (Abstract1,Set[Lincons1]), v2: (Abstract1,Set[Lincons1])): MaybeChanged[(Abstract1,Set[Lincons1])] = {
    val joinedThresholds = v1._2 ++ v2._2
    ApronJoins.combineAbstract1(manager, v1._1, v2._1, joinedThresholds, false).map((_,joinedThresholds))
  }

given Abstract1Widen(using manager: Manager): Widen[(Abstract1,Set[Lincons1])] with
  override def apply(v1: (Abstract1,Set[Lincons1]), v2: (Abstract1,Set[Lincons1])): MaybeChanged[(Abstract1,Set[Lincons1])] =
    val joinedThresholds = v1._2 ++ v2._2
    ApronJoins.combineAbstract1(manager, v1._1, v2._1, joinedThresholds, true).map((_,joinedThresholds))

object ApronJoins:
  def combineAbstract1(manager: Manager, s1: Abstract1, s2: Abstract1, thresholds: Set[Lincons1], widen: Boolean): MaybeChanged[Abstract1] =
    Profiler.addTime("Abstract1.combine") {
      val result = if(s1.getEnvironment.isEqual(s2.getEnvironment)) {
        if(s2.isIncluded(manager, s1)) {
          Unchanged(s1)
        } else {
          if(widen) {
            val s2Copy = s2.joinCopy(manager, s1)
            val result = s1.wideningThreshold(manager, s2Copy, thresholds.filter(lincons => lincons.getEnvironment.isIncluded(s1.getEnvironment)).map(lincons => lincons.extendEnvironmentCopy(s1.getEnvironment)).toArray)
            Changed(result)
          } else {
            val result = s1.joinCopy(manager, s2)
            Changed(result)
          }
        }
      } else if(s1.isBottom(manager)) {
        MaybeChanged(s1, hasChanged = !s2.isBottom(manager))
      } else if(s2.isBottom(manager)) {
        Unchanged(s1)
      } else {

        val env1 = s1.getEnvironment
        val env2 = s2.getEnvironment

        if(env1.getSize == 0) {
          MaybeChanged(s2, env1.getSize != env2.getSize)
        } else if (env2.getSize == 0) {
          Unchanged(s1)
        } else {

          val lce = env1.lce(env2)
          val s1ExtEnv = if (lce.isEqual(env1)) Abstract1(manager,s1) else s1.changeEnvironmentCopy(manager, lce, false)
          val s2ExtEnv = if (lce.isEqual(env2)) Abstract1(manager,s2) else s2.changeEnvironmentCopy(manager, lce, false)

          lazy val model1 = getModel(manager, s1)
          lazy val model2 = getModel(manager, s2)

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
                env1_minus_env2.map(v => ApronExpr.constant(model1.getCoeff(env1.dimOfVar(v)), null).toIntern(lce)),
                null
              )
            else
              s2ExtEnv

          val combined =
            if (widen) {
              // This widens recent variables more precisely.
              // For example, [xr = 1] ∇ [xr = 2] = [ 1 <= xr < infty ]
              combinable2.join(manager, combinable1)
              combinable1.wideningThreshold(manager, combinable2, thresholds.filter(lincons => lincons.getEnvironment.isIncluded(lce)).map(lincons => lincons.extendEnvironmentCopy(lce)).toArray)
//              combinable1.widening(manager, combinable2)
            } else {
              combinable1.joinCopy(manager, combinable2)
            }

          MaybeChanged(combined, !(lce.isEqual(env1) && combined.isIncluded(manager, s1ExtEnv)))
        }
      }

      result
    }

  def getModel(manager: Manager, abs1: Abstract1): apron.Linexpr0 = {
    manager match {
      case _: Polka => abs1.toGenerator(manager).iterator.flatMap {
        case gen if(gen.getGenerator0Ref.kind == Generator1.VERTEX) => Some(gen.getGenerator0Ref.coord)
        case _ => None
      }.next()
      case _: Octagon =>
        Linexpr0(abs1.toBox(manager).map[Coeff](_.inf()), DoubleScalar(0))
//        val env = abs1.getEnvironment
//
//        val coefficents = env.getVars.map(_ => 0d)
//        val d = Array[Double](0d)
//        val constraints = LinearConstraintSet(ArraySeq.unsafeWrapArray(abs1.toLincons(manager).map[LinearConstraint](apronLinCons =>
//          val coeffs = coefficents.clone()
//          for(linTerm <- apronLinCons.getLincons0Ref.getLinterms) {
//            linTerm.coeff.inf().toDouble(d,Mpfr.RNDN)
//            coeffs(linTerm.dim) = d(0)
//          }
//          apronLinCons.getCst.inf().toDouble(d,Mpfr.RNDN)
//          val constraint = LinearConstraint(coeffs, Relationship.GEQ, -d(0))
//          constraint
//        )).asJava)
//
//        val objectiveFunction = LinearObjectiveFunction(coefficents, 0)
//        val solver = SimplexSolver()
//        try {
//          val solution = solver.optimize(objectiveFunction, constraints, MaxIter(1000))
//          Linexpr0(solution.getPointRef.map[Coeff](DoubleScalar(_)), DoubleScalar(0))
//        } catch {
//          case exception:MathIllegalStateException =>
//            println(exception)
//            Linexpr0(abs1.toBox(manager).map[Coeff](_.inf()), DoubleScalar(0))
//        }
      case _: Box => Linexpr0(abs1.toBox(manager).map[Coeff](_.inf()), DoubleScalar(0))
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