package sturdy.apron

import apron.*
import sturdy.util.{Lazy, Profiler}
import sturdy.values.{Combine, Top, Topped, Widen, Widening}

import scala.collection.mutable
import scala.reflect.ClassTag
// import sturdy.apron.Apron.debugJoinWiden
import sturdy.data.{given}
import sturdy.effect.failure.Failure
import sturdy.values.{Changed, Join, MaybeChanged, Unchanged}

given Abstract1Join(using manager: Manager): Join[Abstract1] with
  override def apply(v1: Abstract1, v2: Abstract1): MaybeChanged[Abstract1] =
    ApronJoins.combineAbstract1(manager, v1, v2, false)

given Abstract1Widen(using manager: Manager): Widen[Abstract1] with
  override def apply(v1: Abstract1, v2: Abstract1): MaybeChanged[Abstract1] =
    ApronJoins.combineAbstract1(manager, v1, v2, true)

object ApronJoins:
  def combineAbstract1(manager: Manager, s1: Abstract1, s2: Abstract1, widen: Boolean): MaybeChanged[Abstract1] =
    Profiler.addTime("Abstract1.combine") {
      val result = if(s1.getEnvironment.isEqual(s2.getEnvironment)) {
        if(s2.isIncluded(manager, s1)) {
          Unchanged(s1)
        } else {
          if(widen) {
            val s2Copy = s2.joinCopy(manager, s1)
            Changed(s1.widening(manager, s2Copy))
          } else {
            Changed(s1.joinCopy(manager, s2))
          }
        }
      } else if(s1.isBottom(manager)) {
        MaybeChanged(s1, hasChanged = !s2.isBottom(manager))
      } else if(s2.isBottom(manager)) {
        Unchanged(s1)
      } else {

        val env1 = s1.getEnvironment
        val env2 = s2.getEnvironment

        val lce = env1.lce(env2)

        val model1 = getModel(manager, s1)
        val model2 = getModel(manager, s2)

        val s1ExtEnv = if(lce.isEqual(env1)) s1 else s1.changeEnvironmentCopy(manager, lce, false)
        val s2ExtEnv = if(lce.isEqual(env2)) s2 else s2.changeEnvironmentCopy(manager, lce, false)

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

  def getModel(manager: Manager, abs1: Abstract1): apron.Linexpr0 = {
    manager match {
      case _: Polka => abs1.toGenerator(manager).iterator.flatMap {
        case gen if(gen.getGenerator0Ref.kind == Generator1.VERTEX) => Some(gen.getGenerator0Ref.coord)
        case _ => None
      }.next()
      case _: Octagon =>
        val env = abs1.getEnvironment
        val boxManager = Box()
        // Start with a model that is the lower bounds of single variables
        val model: Abstract1 = Abstract1(boxManager, env, env.getVars, abs1.toBox(manager).map(iv => Interval(iv.inf(), iv.inf())))
        val linearConstraints = abs1.toLincons(manager).filter(cons => !(cons.getLincons0Ref.getSize == 1 || cons.getLincons0Ref.getCoeffs.exists(_.isZero)))

        for(constraint <- linearConstraints; if(! model.satisfy(boxManager, constraint))) {
          val linTerms = constraint.getLinterms
          val a_x = linTerms(0)
          val b_y = linTerms(1)
          val a = if(a_x.getCoefficient.isEqual(1)) 1 else -1
          val b = if(b_y.getCoefficient.isEqual(1)) 1 else -1
          val x_var = a_x.getVariable
          val y_var = b_y.getVariable
          val x_scalar = model.getBound(boxManager, x_var).sup()
          val y_scalar = model.getBound(boxManager, y_var).sup()
          val x_topped = if(x_scalar.isInfty == 0) Topped.Actual(x_scalar) else Topped.Top
          val y_topped = if(y_scalar.isInfty == 0) Topped.Actual(y_scalar) else Topped.Top
          val c = constraint.getCst

          val min_constant = if(c.cmp(DoubleScalar(0)) < 0) c else DoubleScalar(0)

          val neg_a_x = a_x.clone(); neg_a_x.coeff.neg()
          val neg_b_y = b_y.clone(); neg_b_y.coeff.neg()
          val neg_c = c.copy(); neg_c.neg()

          inline def times(a: Int, x: Var): Linterm1 = Linterm1(x, DoubleScalar(a))
          inline def add(a_x: Linterm1, c: Coeff) = model.getBound(boxManager, Linexpr1(env, Array(a_x), c)).inf()

          val (x_new, y_new) = (a, x_topped, b, y_topped) match
            case ( 1, Topped.Top,       1, Topped.Top)       => throw IllegalStateException("Satisfied")
            case ( 1, Topped.Top,      -1, Topped.Top)       => (DoubleScalar(Double.PositiveInfinity), min_constant)
            case ( 1, Topped.Top,       1, Topped.Actual(y)) => throw IllegalStateException("Satisfied")
            case ( 1, Topped.Top,      -1, Topped.Actual(y)) => throw IllegalStateException("Satisfied")
            case (-1, Topped.Top,       1, Topped.Top)       => (min_constant, DoubleScalar(Double.PositiveInfinity))
            case (-1, Topped.Top,      -1, Topped.Top)       => (DoubleScalar(0), min_constant)
            case (-1, Topped.Top,       1, Topped.Actual(y)) => (add(times(1,y_var),c), y)
            case (-1, Topped.Top,      -1, Topped.Actual(y)) => (add(times(-1,y_var),c), y)
            case ( 1, Topped.Actual(x), 1, Topped.Top)       => throw IllegalStateException("Satisfied")
            case ( 1, Topped.Actual(x),-1, Topped.Top)       => (x, add(times(1,x_var),c))
            case ( 1, Topped.Actual(x), 1, Topped.Actual(y)) => (add(times(-1,y_var), neg_c), y)
            case ( 1, Topped.Actual(x),-1, Topped.Actual(y)) => (add(times(1,y_var), neg_c), y)
            case (-1, Topped.Actual(x), 1, Topped.Top)       => throw IllegalStateException("Satisfied")
            case (-1, Topped.Actual(x),-1, Topped.Top)       => (x, add(times(1,x_var), c))
            case (-1, Topped.Actual(x), 1, Topped.Actual(y)) => (add(times(1,y_var), c), y)
            case (-1, Topped.Actual(x),-1, Topped.Actual(y)) => (add(times(-1,y_var), c), y)
            case _ => throw IllegalStateException(s"Unexpected case ($a, $x_topped, $b, $y_topped)")

          model.assign(boxManager, x_var, Texpr1Intern(env, Texpr1CstNode(x_new)), null)
          model.assign(boxManager, y_var, Texpr1Intern(env, Texpr1CstNode(y_new)), null)

          assert(model.satisfy(boxManager, constraint))
        }

        Linexpr0(model.toBox(boxManager).map[Coeff](_.inf()), DoubleScalar(0))
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