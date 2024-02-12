package sturdy.apron

import apron.{Abstract1, Environment, Manager, StringVar}
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

    MaybeChanged(combined, ! (lce.isEqual(env1) && combined.isIncluded(manager, s1ExtEnv)))

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



//  def combineExprs[Addr <: apron.Var](e1: ApronExpr[Addr], e2: ApronExpr[Addr], state: Abstract1, widen: Boolean): MaybeChanged[ApronExpr[Addr]] =
//    val apronManager = state.getCreationManager
////    if (e1.isEqual(e2, state)) {
////      Unchanged(e1)
////    } else {
//
//    val oldBound = state.getBound(e1)
//
//    val e1Intern = e1.toIntern(state)
//    val e2Intern = e2.toIntern(state)
//    val av = new StringVar("$$combineExprs$$")
//    val env = state.getEnvironment
//       .lce(e1Intern.getEnvironment)
//       .lce(e2Intern.getEnvironment)
//       .add(Array(av): Array[apron.Var], null: Array[apron.Var])
//    e1Intern.extendEnvironment(env)
//    e2Intern.extendEnvironment(env)
//    val cs = state.changeEnvironmentCopy(apronManager, env, false)
//    val a1 = cs.assignCopy(apronManager, av, e1Intern, null)
//    cs.assign(apronManager, av, e2Intern, null)
//    val aJoined =
//      if (widen)
//        a1.widening(apronManager, cs)
//      else {
//        a1.join(apronManager, cs)
//        a1

//      }

//    val bound = aJoined.getBound(apronManager, av)
//    MaybeChanged(ApronExpr.num(bound), !bound.isEqual(oldBound))

//    }


// 
//   def combineVars(state: ApronState, v1: ApronVar, v2: ApronVar, widen: Boolean): MaybeChanged[(ApronVar, ApronState)] =
//     val apronManager = state.apronManager
//     if (v1.isEqual(v2, state)) {
//       Unchanged((v1, state))
// //    } else if (v1.av.equals(v2.av)) {
// //      // v1 and v2 are presented by the same apron variable in state, we only need to free one of them.
// //      val v1Freed = state.freed.get(v1.uid)
// //      val v2Freed = state.freed.get(v2.uid)
// //      (v1Freed, v2Freed) match
// //        case (None, None) => Changed((v1, state.copy(state.freed + (v2.uid -> ApronExpr.Var(v1)))))
// //        case (Some(e1), None) =>
// //          if (widen) {
// //            val v1Bound = state.getBound(e1)
// //            val v2Bound = state.cs.getBound(apronManager, v2.av)
// //            val boundChanged = !v1Bound.isEqual(v2Bound)
// //            if (boundChanged) {
// //              val newState = state.copy(state.freed + (v2.uid -> ApronExpr.Constant(v2Bound)))
// //              Changed((v2, newState))
// //            } else {
// //              Unchanged((v1, state))
// //            }
// //          } else {
// //            Changed((v2, state))
// //          }
// //        case (None, Some(_)) => Unchanged((v1, state))
// //        case (Some(e1), Some(e2)) =>
// //          val MaybeChanged(eJoined, changed) = combineExprs(e1, e2, state, widen)
// //          if (changed) {
// //            val newState = state.copy(state.freed + (v1.uid -> eJoined))
// //            Changed((v1, newState))
// //          } else {
// //            Unchanged((v1, state))
// //          }
//     } else {
//       state.freed.get(v1.uid) match
//         case None =>
//           // v1 := v1 join v2
//           val e2intern = v2.expr.toIntern(state)
//           val assigned = state.cs.assignCopy(apronManager, v1.av, e2intern, null)
//           assigned.join(apronManager, state.cs)
//           // if v2 is also free, v2 := v1
//           if (!state.freed.contains(v2.uid)) {
//             val newState = state.copy(assigned).copy(state.freed + (v2.uid -> ApronExpr.Var(v1)))
//             Changed((v1, newState))
//           } else if (!state.cs.isEqual(apronManager, assigned)) {
//             Changed((v1, state.copy(assigned)))
//           } else {
//             Unchanged((v1, state))
//           }
//         case Some(e1) => state.freed.get(v2.uid) match
//           case None =>
//             // v2 := v1 join v2
//             val e1intern = e1.toIntern(state)
//             val assigned = state.cs.assignCopy(apronManager, v2.av, e1intern, null)
//             assigned.join(apronManager, state.cs)
//             if (widen) {
//               // force termination
//               val v1Bound = state.cs.getBound(apronManager, e1intern)
//               val v2Bound = assigned.getBound(apronManager, v2.av)
//               val boundChanged = !v1Bound.isEqual(v2Bound)
//               if (boundChanged) {
//                 val newState = state.copy(assigned, state.freed + (v2.uid -> ApronExpr.Constant(v2Bound)))
//                 Changed((v2, newState))
//               } else {
//                 Unchanged((v1, state))
//               }
//             } else {
//               MaybeChanged((v2, state.copy(assigned)), !state.cs.isEqual(apronManager, assigned))
//             }
//           case Some(e2) =>
//             // join e1 and e2, since neither v1 nor v2 is managed by apron
//             val MaybeChanged(eJoined, changed) = combineExprs(v1.expr, v2.expr, state, widen)
//             if (changed) {
//               val newState = state.copy(state.freed + (v1.uid -> eJoined))
//               Changed((v1, newState))
//             } else {
//               Unchanged((v1, state))
//             }
//     }
//
// 
//   def combineVals[V](vals: ApronVal[V])(joinedApronState: ApronState, v1: vals.Val, v2: vals.Val, widen: Boolean)(using Join[V]): MaybeChanged[(vals.Val, ApronState)] =
//     import vals.Val
//     (v1, v2) match
//       case (v1, null) => Unchanged((v1, joinedApronState))
//       case (null, v2) => Changed((v2, joinedApronState))
//       case (Val.Int(v1), Val.Int(v2)) =>
//         combineVars(joinedApronState, v1, v2, widen).map((v,st) => (Val.Int(v), st))
//       case (Val.Double(v1), Val.Double(v2)) =>
//         combineVars(joinedApronState, v1, v2, widen).map((v,st) => (Val.Double(v), st))
//       case (v1, v2) => Join(v1.asV, v2.asV).map(v => (Val.Other(v), joinedApronState))
// 
//   def combineValLists[V](vals: ApronVal[V])(joinedApronState: ApronState, vs1: List[vals.Val], vs2: List[vals.Val], widen: Boolean)(using Join[V]): MaybeChanged[(List[vals.Val], ApronState)] =
//     var state = joinedApronState
//     var rsChanged = false
//     val rs = vs1.zip(vs2).map {
//       case (v1, v2) =>
//         val MaybeChanged((v, st), changed) = combineVals(vals)(state, v1, v2, widen)
//         rsChanged |= changed
//         state = st
//         v
//     }
//     MaybeChanged((rs, state), rsChanged)
// 
//   def combineAdditionalVars(joinedApronState: ApronState, additionalVars1: Map[String, ApronVar], additionalVars2: Map[String, ApronVar], widen: Boolean) =
//     var state = joinedApronState
//     var varsChanged = false
//     val additionalVars = combineMaps(additionalVars1, additionalVars2, { (v1, v2) =>
//       val MaybeChanged((v, s), ch) = combineVars(state, v1, v2, widen)
//       state = s
//       varsChanged |= ch
//       if (ch) {
//         println(s"join additional $v1 + $v2 = $v")
//       }
//       v
//     })
//     MaybeChanged((additionalVars, state), varsChanged || additionalVars.size != additionalVars1.size)
// }
// 