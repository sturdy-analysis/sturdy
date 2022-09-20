package sturdy.effect.callframe

import apron.Texpr1Intern
import apron.{Environment, Interval, Texpr1VarNode, Tcons1, StringVar, Manager, Abstract1}
import org.eclipse.collections.api.factory.BiMaps
import org.eclipse.collections.api.bimap.{ImmutableBiMap, MutableBiMap, BiMap}
import sturdy.apron.Apron
import sturdy.apron.ApronAllocationSite
import sturdy.apron.ApronExpr
import sturdy.apron.ApronState
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.ComputationJoiner
import sturdy.effect.SturdyFailure
import sturdy.effect.TrySturdy
import sturdy.values.Finite
import sturdy.values.{MaybeChanged, Unchanged, Changed}
import sturdy.values.Widening
import sturdy.values.{Widen, Join, Combine}

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
  extends DecidableMutableCallFrame[Data, Var, V](initData, initVars) :

  import apron.*

  private var allocatedVars: Set[alloc.Var] = Set()
  private var boundVars: Map[Int, Val] = Map()

  enum Val:
    case Int(v: alloc.Var)
    case Double(v: alloc.Var)
    case Other(v: V)

    def asV: V = this match
      case Int(v) => makeIntVal(ApronExpr.Var(v))
      case Double(v) => makeIntVal(ApronExpr.Var(v))
      case Other(v) => v
      case null => null.asInstanceOf[V]

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

  /** Changes state */
  def joinVal(state: Abstract1): Join[Val] = {
    case (v1, null) => Unchanged(v1)
    case (null, v2) => Changed(v2)
    case (Val.Int(v1), Val.Int(v2)) =>
      if (v1 != v2) {
        val av1 = v1.getOrElse(throw new IllegalStateException(s"Cannot widen with freed variable $v1"))
        val v2Intern = new Texpr1Intern(state.getEnvironment, v2.node)
        val assigned = state.assignCopy(apronManager, av1, v2Intern, null)
        state.join(apronManager, assigned)
      }
      Unchanged(Val.Int(v1))
    case (Val.Double(v1), Val.Double(v2)) =>
      if (v1 != v2) {
        val av1 = v1.getOrElse(throw new IllegalStateException(s"Cannot widen with freed variable $v1"))
        val v2Intern = new Texpr1Intern(state.getEnvironment, v2.node)
        val assigned = state.assignCopy(apronManager, av1, v2Intern, null)
        state.join(apronManager, assigned)
      }
      Unchanged(Val.Int(v1))
    case (v1, v2) => Join(v1.asV, v2.asV).map(Val.Other.apply)
  }

  /** Changes state */
  def widenVal(state: Abstract1): Widen[Val] = {
    case(v1, null) => Unchanged(v1)
    case(null, v2) => Changed(v2)
    case (Val.Int(v1), Val.Int(v2)) =>
      if (v1 != v2) {
        val av1 = v1.getOrElse(throw new IllegalStateException(s"Cannot widen with freed variable $v1"))
        val v2Intern = new Texpr1Intern(state.getEnvironment, v2.node)
        val assigned = state.assignCopy(apronManager, av1, v2Intern, null)
        state.join(apronManager, assigned)
      }
      Unchanged(Val.Int(v1))
    case (Val.Double(v1), Val.Double(v2)) =>
      if (v1 != v2) {
        val av1 = v1.getOrElse(throw new IllegalStateException(s"Cannot widen with freed variable $v1"))
        val v2Intern = new Texpr1Intern(state.getEnvironment, v2.node)
        val assigned = state.assignCopy(apronManager, av1, v2Intern, null)
        state.join(apronManager, assigned)
      }
      Unchanged(Val.Int(v1))
    case (v1, v2) => Widen(v1.asV, v2.asV).map(Val.Other.apply)
  }


  override def setVars(newVars: Iterable[(Var, Option[V])]): Unit = {
    names = newVars.zipWithIndex.map(t => t._1._1 -> t._2).toMap

    boundVars = Map()
    val newApronAssignments: ListBuffer[(alloc.Var, ApronExpr)] = ListBuffer()

    newVars.zipWithIndex.foreach {
      case ((_, None), ix) =>
        boundVars += ix -> null
      case ((x, Some(v)), ix) =>
        getIntVal(v) match
          case Some(exp) =>
            val av = addIntVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
            allocatedVars += av
            newApronAssignments += av -> exp
            boundVars += ix -> Val.Int(av)
          case None => getDoubleVal(v) match
            case Some(exp) =>
              val av = addDoubleVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
              allocatedVars += av
              newApronAssignments += av -> exp
              boundVars += ix -> Val.Double(av)
            case None =>
              boundVars += ix -> Val.Other(v)
    }

    newApronAssignments.foreach { case (av, exp) => assign(av, exp) }
  }

  setVars(initVars)

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[V])])(f: => A): A = {
    val snapData = this._data
    val snapNames = this.names
    val snapAllocatedVars = this.allocatedVars
    val snapBoundVars = this.boundVars
    this._data = d
    this.allocatedVars = Set()
    setVars(vars)
    try f finally {
      this._data = snapData
      this.names = snapNames
      this.allocatedVars.foreach(apron.freeVariable)
      this.allocatedVars = snapAllocatedVars
      this.boundVars = snapBoundVars
    }
  }

  override def data: Data = _data

  override def getLocal(x: Int): JOptionC[V] = boundVars.get(x) match
    case None => JOptionC.none
    case Some(e) => e match
      case Val.Int(av) => JOptionC.some(makeIntVal(ApronExpr.Var(av)))
      case Val.Double(av) => JOptionC.some(makeDoubleVal(ApronExpr.Var(av)))
      case Val.Other(v) => JOptionC.some(v)
      case null => JOptionC.none

  override def getLocalByName(x: Var): JOptionC[V] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => getLocal(ix)

  def setLocal(x: Int, name: Var, v: V): JOptionC[Unit] = boundVars.get(x) match
    case None => JOptionC.none
    case Some(oldVal) => getIntVal(v) match
      case Some(exp) =>
        oldVal match
          case Val.Int(av) =>
            apron.assign(av, exp)
          case _ =>
            val intVar = addIntVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$name"))
            allocatedVars += intVar
            apron.assign(intVar, exp)
            boundVars += x -> Val.Int(intVar)
      case None => getDoubleVal(v) match
        case Some(exp) =>
          oldVal match
            case Val.Double(av) =>
              apron.assign(av, exp)
            case _ =>
              val doubleVar = addDoubleVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$name"))
              allocatedVars += doubleVar
              apron.assign(doubleVar, exp)
              boundVars += x -> Val.Double(doubleVar)
        case None =>
          boundVars += x -> Val.Other(v)
      JOptionC.some(())

  /** Assumes Var =:= Int. */
  override def setLocal(x: Int, v: V): JOptionC[Unit] =
    setLocal(x, x.asInstanceOf[Var], v)

  override def setLocalByName(x: Var, v: V): JOptionC[Unit] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => setLocal(ix, x, v)

  type State = (apron.State, Map[Int, Val])

  /** state contains the constraints for the current frame only */
  override def getState: State = (apron.getState, boundVars)

  override def setState(st: State): Unit =
    apron.setState(st._1)
    this.boundVars = st._2

  override def join: Join[State] = {
    case ((s1, vars1), (s2, vars2)) =>
      val MaybeChanged(as, changed) = apron.join(s1, s2)
      val state = new Abstract1(apronManager, as.s)
      val manager = as.apronManager
      val MaybeChanged(vars, varsChanged) = JoinMap(using joinVal(state))(vars1, vars2)
      val newApronState = new ApronState(state, manager)
      MaybeChanged((newApronState, vars), changed || varsChanged || !as.s.isEqual(manager, state))
  }

  override def widen: Widen[State] = { case ((s1, vars1), (s2, vars2)) =>
    val MaybeChanged(as, changed) = apron.widen(s1, s2)
    val state = new Abstract1(apronManager, as.s)
    val manager = as.apronManager
    val MaybeChanged(vars, varsChanged) = WidenFiniteKeyMap(using widenVal(state), new Finite[Int]{})(vars1, vars2)
    val newApronState = new ApronState(state, manager)
    if (Apron.debugWiden)
      println(
        s"""Widening call frame
           |  vars1 = $vars1
           |  vars2 = $vars2
           |  vars = $vars
           |  changed = $varsChanged""".stripMargin)
    MaybeChanged((newApronState, vars), changed || varsChanged || !as.s.isEqual(manager, state))
  }


  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] =
    apron.makeComputationJoiner
