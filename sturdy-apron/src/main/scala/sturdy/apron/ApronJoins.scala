package sturdy.apron

import apron.{Abstract1, Manager}
import sturdy.values.{Join, MaybeChanged, Changed, Unchanged}

class ApronJoins(val apron: Apron) {

  import apron.{alloc, apronManager, getFreedReference, freeReference, inScope}

  def combineVars(state: ApronState, v1: ApronVar, v2: ApronVar, widen: Boolean): MaybeChanged[(ApronVar, ApronState)] =
    val joinedApronState = state.s
    if (v1 == v2)
      Unchanged((v1, state))
    else getFreedReference(v1) match
      case None =>
        // join v2 into constraints for v1
        val e2intern = v2.expr.toIntern(apron)
        val assigned = joinedApronState.assignCopy(apronManager, v1.av, e2intern, null)
        assigned.join(apronManager, joinedApronState)
        val changed = !joinedApronState.isEqual(apronManager, assigned)
        if (inScope(v2))
          freeReference(v2, ApronExpr.Var(v1))
        if (changed) {
          Changed((v1, new ApronState(apronManager, assigned)))
        } else {
          Unchanged((v1, state))
        }
      case Some(e1) => getFreedReference(v2) match
        case None =>
          // join e1 into constraints for v2
          val e1intern = e1.toIntern(apron)
          val assigned = joinedApronState.assignCopy(apronManager, v2.av, e1intern, null)
          assigned.join(apronManager, joinedApronState)
          val assignedState = new ApronState(apronManager, assigned)
          if (widen) {
            // force termination
            val v2Bound = assigned.getBound(apronManager, v2.av)
            val v1Bound = joinedApronState.getBound(apronManager, e1intern)
            val boundChanged = !v1Bound.isEqual(v2Bound)
            if (boundChanged) {
              freeReference(v2, ApronExpr.Constant(v2Bound))
              Changed((v2, assignedState))
            } else {
              Unchanged((v1, assignedState))
            }
          } else {
            MaybeChanged((v2, assignedState), !joinedApronState.isEqual(apronManager, assigned))
          }
        case Some(e2) =>
          // join e1 and e2, since neither v1 nor v2 is managed by apron
          val MaybeChanged(eJoined, changed) = combineExprs(v1.expr, v2.expr, state, widen = false)
          if (changed) {
            freeReference(v1, eJoined)
            Changed((v1, state))
          } else {
            Unchanged((v1, state))
          }

  def combineExprs(e1: ApronExpr, e2: ApronExpr, apronState: ApronState, widen: Boolean): MaybeChanged[ApronExpr] =
    if (e1 == e2) {
      Unchanged(e1)
    } else {
      apron.withLocal(apronState)(apron.withTemporaryIntVariable { x =>
        val oldBound = apron.getBound(e1)

        apron.initializeVar(x)
        val e1Intern = e1.toIntern(apron)
        val a1 = apronState.s.assignCopy(apronManager, x.av, e1Intern, null)
        val e2Intern = e2.toIntern(apron)
        val a2 = apronState.s.assignCopy(apronManager, x.av, e2Intern, null)
        val aJoined =
          if (widen)
            a1.widening(apronManager, a2)
          else {
            a1.join(apronManager, a2)
            a1
          }

        val xBound = aJoined.getBound(apronManager, x.av)
        if (Apron.debugJoinWiden)
          println(s"Join values $e1 and $e2 is $x, widen = $widen")
        MaybeChanged(x.expr, !xBound.isEqual(oldBound))
      })
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
        val MaybeChanged((v, st), changed) = apron.joins.combineVals(vals)(state, v1, v2, widen)
        rsChanged |= changed
        state = st
        v
    }
    MaybeChanged((rs, state), rsChanged)
}
