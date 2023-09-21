package sturdy.apron

import apron.{Abstract1, Manager, StringVar}
import sturdy.apron.Apron.debugJoinWiden
import sturdy.data.combineMaps
import sturdy.effect.failure.Failure
import sturdy.values.{Changed, Join, MaybeChanged, Unchanged}

object ApronJoins {

  def combineApronStates(s1: Abstract1, s2: Abstract1, widen: Boolean)(using Failure): MaybeChanged[Abstract1] = {
    // TODO review this code on first widening in cfgloop.tip
    if (debugJoinWiden) {
      println(
        s"""${if (widen) "Widening" else "Joining"} apron
           |  s1 = $s1
           |  s2 = $s2""".stripMargin)
    }

    val vars1 = s1.getEnvironment.getVars.toSet
    val vars2 = s2.getEnvironment.getVars.toSet
    val inboth = (vars1 intersect vars2).toArray

    val apronManager = s1.getCreationManager
    val lce = s1.getEnvironment.lce(s2.getEnvironment)
    val combinable1 = s1.changeEnvironmentCopy(apronManager, lce, false)
    val combinable2 = s2.changeEnvironmentCopy(apronManager, lce, false)
    val combined =
      if (widen)
        combinable1.widening(apronManager, combinable2)
      else
        combinable1.joinCopy(apronManager, combinable2)

    if (debugJoinWiden) {
      println(
        s"""  joined = $combined""".stripMargin)
    }
    if (Apron.debugJoinWiden && !combinable1.isIncluded(apronManager, combined))
      throw new IllegalStateException(s"$combinable1 not included in $combined")
    if (Apron.debugJoinWiden && !combinable2.isIncluded(apronManager, combined))
      throw new IllegalStateException(s"$combinable2 not included in $combined")

    val s1Only = combinable1.forgetCopy(apronManager, inboth, false)
    val s2Only = combinable2.forgetCopy(apronManager, inboth, false)
    combined.meet(apronManager, s1Only)
    combined.meet(apronManager, s2Only)
    val changed = !combined.isEqual(apronManager, combinable1)
    if (debugJoinWiden) {
      println(
        s"""  changed = $changed""".stripMargin)
    }
    if (debugJoinWiden && changed && combined.toString(apronManager) == combinable1.toString(apronManager))
      throw new IllegalStateException()
    if (combined.isBottom(apronManager))
      Failure(Apron.Bottom, s"combine($s1, $s2) is bottom")
    MaybeChanged(combined, changed)
  }

  def combineVars(state: ApronState, v1: ApronVar, v2: ApronVar, widen: Boolean): MaybeChanged[(ApronVar, ApronState)] =
    val apronManager = state.apronManager
    if (v1.isEqual(v2, state)) {
      Unchanged((v1, state))
//    } else if (v1.av.equals(v2.av)) {
//      // v1 and v2 are presented by the same apron variable in state, we only need to free one of them.
//      val v1Freed = state.freed.get(v1.uid)
//      val v2Freed = state.freed.get(v2.uid)
//      (v1Freed, v2Freed) match
//        case (None, None) => Changed((v1, state.copy(state.freed + (v2.uid -> ApronExpr.Var(v1)))))
//        case (Some(e1), None) =>
//          if (widen) {
//            val v1Bound = state.getBound(e1)
//            val v2Bound = state.cs.getBound(apronManager, v2.av)
//            val boundChanged = !v1Bound.isEqual(v2Bound)
//            if (boundChanged) {
//              val newState = state.copy(state.freed + (v2.uid -> ApronExpr.Constant(v2Bound)))
//              Changed((v2, newState))
//            } else {
//              Unchanged((v1, state))
//            }
//          } else {
//            Changed((v2, state))
//          }
//        case (None, Some(_)) => Unchanged((v1, state))
//        case (Some(e1), Some(e2)) =>
//          val MaybeChanged(eJoined, changed) = combineExprs(e1, e2, state, widen)
//          if (changed) {
//            val newState = state.copy(state.freed + (v1.uid -> eJoined))
//            Changed((v1, newState))
//          } else {
//            Unchanged((v1, state))
//          }
    } else {
      state.freed.get(v1.uid) match
        case None =>
          // v1 := v1 join v2
          val e2intern = v2.expr.toIntern(state)
          val assigned = state.cs.assignCopy(apronManager, v1.av, e2intern, null)
          assigned.join(apronManager, state.cs)
          // if v2 is also free, v2 := v1
          if (!state.freed.contains(v2.uid)) {
            val newState = state.copy(assigned).copy(state.freed + (v2.uid -> ApronExpr.Var(v1)))
            Changed((v1, newState))
          } else if (!state.cs.isEqual(apronManager, assigned)) {
            Changed((v1, state.copy(assigned)))
          } else {
            Unchanged((v1, state))
          }
        case Some(e1) => state.freed.get(v2.uid) match
          case None =>
            // v2 := v1 join v2
            val e1intern = e1.toIntern(state)
            val assigned = state.cs.assignCopy(apronManager, v2.av, e1intern, null)
            assigned.join(apronManager, state.cs)
            if (widen) {
              // force termination
              val v1Bound = state.cs.getBound(apronManager, e1intern)
              val v2Bound = assigned.getBound(apronManager, v2.av)
              val boundChanged = !v1Bound.isEqual(v2Bound)
              if (boundChanged) {
                val newState = state.copy(assigned, state.freed + (v2.uid -> ApronExpr.Constant(v2Bound)))
                Changed((v2, newState))
              } else {
                Unchanged((v1, state))
              }
            } else {
              MaybeChanged((v2, state.copy(assigned)), !state.cs.isEqual(apronManager, assigned))
            }
          case Some(e2) =>
            // join e1 and e2, since neither v1 nor v2 is managed by apron
            val MaybeChanged(eJoined, changed) = combineExprs(v1.expr, v2.expr, state, widen)
            if (changed) {
              val newState = state.copy(state.freed + (v1.uid -> eJoined))
              Changed((v1, newState))
            } else {
              Unchanged((v1, state))
            }
    }

  def combineExprs(e1: ApronExpr, e2: ApronExpr, state: ApronState, widen: Boolean): MaybeChanged[ApronExpr] =
    val apronManager = state.apronManager
    if (e1.isEqual(e2, state)) {
      Unchanged(e1)
    } else {
      val oldBound = state.getBound(e1)

      val e1Intern = e1.toIntern(state, allowOpen = false)
      val e2Intern = e2.toIntern(state, allowOpen = false)
      val av = new StringVar("$$combineExprs$$")
      val env = state.apronEnv
        .lce(e1Intern.getEnvironment)
        .lce(e2Intern.getEnvironment)
        .add(Array(av): Array[apron.Var], null: Array[apron.Var])
      e1Intern.extendEnvironment(env)
      e2Intern.extendEnvironment(env)
      val cs = state.cs.changeEnvironmentCopy(apronManager, env, false)
      val a1 = cs.assignCopy(apronManager, av, e1Intern, null)
      cs.assign(apronManager, av, e2Intern, null)
      val aJoined =
        if (widen)
          a1.widening(apronManager, cs)
        else {
          a1.join(apronManager, cs)
          a1
        }

      val bound = aJoined.getBound(apronManager, av)
      if (Apron.debugJoinWiden)
        println(s"Join values $e1 and $e2 is $bound, widen = $widen")
      MaybeChanged(ApronExpr.num(bound), !bound.isEqual(oldBound))
    }

  def combineVals[V](vals: ApronVal[V])(joinedApronState: ApronState, v1: vals.Val, v2: vals.Val, widen: Boolean)(using Join[V]): MaybeChanged[(vals.Val, ApronState)] =
    import vals.Val
    (v1, v2) match
      case (v1, null) => Unchanged((v1, joinedApronState))
      case (null, v2) => Changed((v2, joinedApronState))
      case (Val.Int(v1), Val.Int(v2)) =>
        combineVars(joinedApronState, v1, v2, widen).map((v,st) => (Val.Int(v), st))
      case (Val.Double(v1), Val.Double(v2)) =>
        combineVars(joinedApronState, v1, v2, widen).map((v,st) => (Val.Double(v), st))
      case (v1, v2) => Join(v1.asV, v2.asV).map(v => (Val.Other(v), joinedApronState))

  def combineValLists[V](vals: ApronVal[V])(joinedApronState: ApronState, vs1: List[vals.Val], vs2: List[vals.Val], widen: Boolean)(using Join[V]): MaybeChanged[(List[vals.Val], ApronState)] =
    var state = joinedApronState
    var rsChanged = false
    val rs = vs1.zip(vs2).map {
      case (v1, v2) =>
        val MaybeChanged((v, st), changed) = combineVals(vals)(state, v1, v2, widen)
        rsChanged |= changed
        state = st
        v
    }
    MaybeChanged((rs, state), rsChanged)

  def combineAdditionalVars(joinedApronState: ApronState, additionalVars1: Map[String, ApronVar], additionalVars2: Map[String, ApronVar], widen: Boolean) =
    var state = joinedApronState
    var varsChanged = false
    val additionalVars = combineMaps(additionalVars1, additionalVars2, { (v1, v2) =>
      val MaybeChanged((v, s), ch) = combineVars(state, v1, v2, widen)
      state = s
      varsChanged |= ch
      if (ch) {
        println(s"join additional $v1 + $v2 = $v")
      }
      v
    })
    MaybeChanged((additionalVars, state), varsChanged || additionalVars.size != additionalVars1.size)
}
