package sturdy.effect.callframe

import apron.Texpr1Intern
import apron.{Abstract1, Environment, Interval, Manager, StringVar, Tcons1, Texpr1VarNode}
import org.eclipse.collections.api.factory.BiMaps
import org.eclipse.collections.api.bimap.{BiMap, ImmutableBiMap, MutableBiMap}
import sturdy.apron.{Apron, ApronAllocationSite, ApronExpr, ApronState, ApronVal, ApronVar}
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.ComputationJoiner
import sturdy.effect.SturdyFailure
import sturdy.effect.TrySturdy
import sturdy.values.Finite
import sturdy.values.{Changed, MaybeChanged, Unchanged}
import sturdy.values.Widening
import sturdy.values.{Combine, Join, Widen}

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import reflect.Selectable.reflectiveSelectable
import scala.language.reflectiveCalls

@FunctionalInterface
trait CallFrameSite[D] extends (D => String):
  def apply(d: D): String

given CallFrameSite[String] = identity

class ApronCallFrame[Data, Var, V](val apron: Apron,
                                   initData: Data,
                                   getIntVal: V => Option[ApronExpr],
                                   getDoubleVal: V => Option[ApronExpr],
                                   makeIntVal: ApronExpr => V,
                                   makeDoubleVal: ApronExpr => V,
                                   initVars: Iterable[(Var, Option[V])] = Iterable.empty)
                                  (using site: CallFrameSite[Data])
                                  (using Join[V], Widen[V], ClassTag[V])
  extends MutableCallFrame[Data, Var, V, NoJoin], DecidableCallFrame[Data, Var, V]:

  val vals: ApronVal[V] = new ApronVal(apron, makeIntVal, makeDoubleVal)
  import vals.Val

  import apron.{apronManager, alloc}

  protected var _data: Data = initData
  protected var vars: Array[Val] = _
  protected var names: Map[Var, Int] = _

  setVars(initVars)

  private def allocatedVars: Iterable[ApronVar] = vars.toSeq.flatMap(Val.getVar)

//    override def equals(obj: Any): Boolean = (this, obj) match
//      case (Int(v1), Int(v2)) => apron.getBound(v1) == apron.getBound(v2)
//      case (Double(v1), Double(v2)) => apron.getBound(v1) == apron.getBound(v2)
//      case (Other(v1), Other(v2)) => v1 == v2
//      case _ => false
//
//    override def hashCode: scala.Int = this match
//      case Int(v) => apron.getBound(v).hashCode
//      case Double(v) => apron.getBound(v).hashCode
//      case Other(v) => v.hashCode

  def setVars(newVars: Iterable[(Var, Option[V])]): Unit = {
    names = newVars.zipWithIndex.map(t => t._1._1 -> t._2).toMap

    vars = Array.ofDim(newVars.size)
    val newApronAssignments: ListBuffer[(alloc.Var, ApronExpr)] = ListBuffer()

    newVars.zipWithIndex.foreach {
      case ((_, None), ix) =>
        vars(ix) = null
      case ((x, Some(v)), ix) =>
        getIntVal(v) match
          case Some(exp) =>
            val av = apron.addIntVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
            newApronAssignments += av -> exp
            vars(ix) = Val.Int(av)
          case None => getDoubleVal(v) match
            case Some(exp) =>
              val av = apron.addDoubleVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
              newApronAssignments += av -> exp
              vars(ix) = Val.Double(av)
            case None =>
              vars(ix) = Val.Other(v)
    }

    newApronAssignments.foreach { case (av, exp) => apron.assign(av, exp) }
  }

  setVars(initVars)

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[V])])(f: => A): A = {
    val snapData = this._data
    val snapNames = this.names
    val snapVars = this.vars
    this._data = d
//    if (Apron.debugAlloc)
//      println(apron.alloc)
    setVars(vars)
    val vs = this.allocatedVars
    try f finally {
      this._data = snapData
      this.names = snapNames
      vs.foreach(v => apron.freeVariable(v.asInstanceOf[apron.alloc.Var]))
      this.vars = snapVars
//      if (Apron.debugAlloc)
//        println(apron.alloc)
    }
  }

  override def data: Data = _data

  override def getLocal(ix: Int): JOptionC[V] =
    if (ix >= 0 && ix < vars.length) {
      val v = vars(ix)
      v match
        case Val.Int(av) => JOptionC.some(makeIntVal(ApronExpr.Var(av)))
        case Val.Double(av) => JOptionC.some(makeDoubleVal(ApronExpr.Var(av)))
        case Val.Other(v) => JOptionC.some(v)
        case null => JOptionC.none
    }
    else
      JOptionC.none

  override def getLocalByName(x: Var): JOptionC[V] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => getLocal(ix)


  def setLocal(ix: Int, name: Var, v: V): JOptionC[Unit] =
    if (ix >= 0 && ix < vars.length) {
      val oldVal = vars(ix)
      getIntVal(v) match
        case Some(exp) =>
          oldVal match
            case Val.Int(av) =>
              apron.assign(av.asInstanceOf[apron.alloc.Var], exp).foreach(av2 => vars(ix) = Val.Int(av2))
            case Val.Double(av) =>
              apron.freeVariable(av.asInstanceOf[apron.alloc.Var])
              addNewVariable(ix, isInt = true, name, exp)
            case _ =>
              addNewVariable(ix, isInt = true, name, exp)
        case None => getDoubleVal(v) match
          case Some(exp) =>
            oldVal match
              case Val.Double(av) =>
                apron.assign(av.asInstanceOf[apron.alloc.Var], exp).foreach(av2 => vars(ix) = Val.Double(av2))
              case Val.Int(av) =>
                apron.freeVariable(av.asInstanceOf[apron.alloc.Var])
                addNewVariable(ix, isInt = false, name, exp)
              case _ =>
                addNewVariable(ix, isInt = false, name, exp)
          case None =>
            Val.getVar(oldVal).foreach(v => apron.freeVariable(v.asInstanceOf[apron.alloc.Var]))
            vars(ix) = Val.Other(v)
      JOptionC.some(())
    } else {
      JOptionC.none
    }

  private def addNewVariable(ix: Int, isInt: Boolean, name: Var, exp: ApronExpr): Unit = {
    val varSite = ApronAllocationSite.LocalVar(s"${site(_data)}:$name")
    val v = if (isInt) apron.addIntVariable(varSite) else apron.addDoubleVariable(varSite)
    val x = apron.assign(v, exp).getOrElse(v)
    if (isInt)
      vars(ix) = Val.Int(x)
    else
      vars(ix) = Val.Double(x)
  }

  override def setLocal(ix: Int, v: V): JOptionC[Unit] =
    setLocal(ix, names.find(_._2 == ix).get._1, v)

  override def setLocalByName(x: Var, v: V): JOptionC[Unit] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => setLocal(ix, x, v)

  type State = (ApronState, List[vals.Val])

  private def setVarsConsistentWithState(vars: Array[Val]): Unit =
    this.vars = vars.map {
      case null => null
      case v@Val.Int(av) => if (!apron.inScope(av)) Val.Int(apron.alloc.freshReference(av.asInstanceOf[apron.alloc.Var])) else v
      case v@Val.Double(av) => if (!apron.inScope(av)) Val.Double(apron.alloc.freshReference(av.asInstanceOf[apron.alloc.Var])) else v
      case v => v
    }

  override def getState: State =
    val frozenVars = vars.toList
    (apron.getState, frozenVars)

  override def setState(st: State): Unit =
//    if (Apron.debugAlloc)
//      println(apron.alloc)
    apron.setState(st._1)
    setVarsConsistentWithState(st._2.toArray)
    if (Apron.debugJoinWiden || Apron.debugAlloc)
      println(s"Restored ApronCallFrame state ${vars.toList} in $apron")
//    if (Apron.debugAlloc)
//      println(apron.alloc)

  def combine(st1: State, st2: State, widen: Boolean): MaybeChanged[State] =
    val MaybeChanged(state, apronChanged) = if (widen) apron.widen(st1._1, st2._1) else apron.join(st1._1, st2._1)
    val MaybeChanged((vars, combinedState), varsChanged) = apron.joins.combineValLists(vals)(state, st1._2, st2._2, widen)
    if (Apron.debugJoinWiden)
      println(
        s"""${if (widen) "Widening" else "Joining"} apron call frame
           |  vars1 = ${st1._2}
           |  vars2 = ${st2._2}
           |  vars  = $vars
           |  apron = $combinedState
           |  apronChanged = $apronChanged
           |  varsChanged = $varsChanged""".stripMargin)

    MaybeChanged((combinedState, vars), apronChanged || varsChanged)

  override def join: Join[State] = combine(_, _, widen = false)

  override def widen: Widen[State] = combine(_, _, widen = true)

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new CallFrameJoiner[A])

  protected class CallFrameJoiner[A] extends apron.ApronComputationJoiner[A] {
    private val snapshot = vars.toList
    private var fVars: Array[Val] = _

    override def inbetween(): Unit =
      super.inbetween()
      fVars = vars
      val snapshotWithNewVars = snapshot.zip(fVars).map { case (null, v: (Val.Int | Val.Double)) => v; case (v, _) => v }
      setVarsConsistentWithState(snapshotWithNewVars.toArray)

    override def retainNone(): Unit =
      super.retainNone()
      setVarsConsistentWithState(snapshot.toArray)

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      super.retainFirst(fRes)
      setVarsConsistentWithState(fVars)

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      super.retainSecond(gRes)

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      if (vars.length != fVars.length)
        throw IllegalStateException()

      super.retainBoth(fRes, gRes)

      val fVarsStr = if (Apron.debugJoinWiden) fVars.toList.toString else ""
      val varsStr = if (Apron.debugJoinWiden) vars.toList.toString else ""

      val MaybeChanged((joinedVars, st), _) = apron.joins.combineValLists(vals)(apron.getState, fVars.toList, vars.toList, widen = false)
      apron.setState(st)

      if (Apron.debugJoinWiden) {
        println(
          s"""Computation joiner call frame
             |  vars1 = $fVarsStr
             |  vars2 = $varsStr
             |  vars = $joinedVars
             |  apron = $apron""".stripMargin)
      }

      setVarsConsistentWithState(joinedVars.toArray)
  }

//  class ApronCallFrameState(val as: ApronState, val vars: List[Val]):
//    override def equals(obj: Any): Boolean = obj match
//      case that: ApronCallFrameState =>
//        this.vars.size == that.vars.size && this.as == that.as && this.vars.zip(that.vars).forall {
//          case (null, null) => true
//          case (Val.Int(v1), Val.Int(v2)) => v1.isEqual(v2, apron)
//          case (Val.Double(v1), Val.Double(v2)) => v1.isEqual(v2, apron)
//          case (Val.Other(v1), Val.Other(v2)) => v1 == v2
//          case _ => false
//        }
//      case _ =>
//        false
//
//    override def hashCode(): Int =
//      s.hashCode(apronManager)
//
//    override def toString: String =
//      "(env = " + s.getEnvironment.toString + ", state = " + s.toString(apronManager) + ")"
//
//
//
