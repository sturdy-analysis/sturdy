package sturdy.effect.callframe

import apron.Texpr1Intern
import apron.{Abstract1, Environment, Interval, Manager, StringVar, Tcons1, Texpr1VarNode}
import org.eclipse.collections.api.factory.BiMaps
import org.eclipse.collections.api.bimap.{BiMap, ImmutableBiMap, MutableBiMap}
import sturdy.apron.Apron
import sturdy.apron.ApronAllocationSite
import sturdy.apron.ApronExpr
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

  import apron.{apronManager, alloc}

  protected var _data: Data = initData
  protected var vars: Array[Val] = _
  protected var names: Map[Var, Int] = _

  setVars(initVars)

  private def allocatedVars: Iterable[alloc.Var] = vars.toSeq.flatMap(Val.getVar)

  enum Val:
    case Int(v: alloc.Var)
    case Double(v: alloc.Var)
    case Other(v: V)

    def asV: V = this match
      case Int(v) => makeIntVal(ApronExpr.Var(v))
      case Double(v) => makeIntVal(ApronExpr.Var(v))
      case Other(v) => v
      case null => null.asInstanceOf[V]

  object Val:
    def getVar(v: Val): Option[alloc.Var] = v match
      case Val.Int(v) => Some(v)
      case Val.Double(v) => Some(v)
      case _ => None

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
      if (v1 == v2) {
        if (v1.refCount < v2.refCount)
          Changed(Val.Int(v2))
        else
          Unchanged(Val.Int(v1))
      } else {
        val av1 = v1.getOrElse(throw new IllegalStateException(s"Cannot join with freed variable $v1"))
        val v2Intern = new Texpr1Intern(state.getEnvironment, v2.node)
        val assigned = state.assignCopy(apronManager, av1, v2Intern, null)
        state.join(apronManager, assigned)
        Unchanged(Val.Int(v1))
      }

    case (Val.Double(v1), Val.Double(v2)) =>
      if (v1 == v2) {
        if (v1.refCount < v2.refCount)
          Changed(Val.Double(v2))
        else
          Unchanged(Val.Double(v1))
      } else {
        val av1 = v1.getOrElse(throw new IllegalStateException(s"Cannot join with freed variable $v1"))
        val v2Intern = new Texpr1Intern(state.getEnvironment, v2.node)
        val assigned = state.assignCopy(apronManager, av1, v2Intern, null)
        state.join(apronManager, assigned)
        Unchanged(Val.Double(v1))
      }
    case (v1, v2) => Join(v1.asV, v2.asV).map(Val.Other.apply)
  }

  /** Changes state */
  def widenVal(state: Abstract1): Widen[Val] = {
    case(v1, null) => Unchanged(v1)
    case(null, v2) => Changed(v2)
    case (Val.Int(v1), Val.Int(v2)) =>
      if (v1 == v2) {
        if (v1.refCount < v2.refCount)
          Changed(Val.Int(v2))
        else
          Unchanged(Val.Int(v1))
      } else {
//        apron.joinValues(v1, v2, false)
        val av1 = v1.getOrElse(throw new IllegalStateException(s"Cannot widen with freed variable $v1"))
        val v2Intern = new Texpr1Intern(state.getEnvironment, v2.node)
        val assigned = state.assignCopy(apronManager, av1, v2Intern, null)
        state.join(apronManager, assigned)
        Unchanged(Val.Int(v1))
      }
    case (Val.Double(v1), Val.Double(v2)) =>
      if (v1 == v2) {
        if (v1.refCount < v2.refCount)
          Changed(Val.Double(v2))
        else
          Unchanged(Val.Double(v1))
      } else {
        val av1 = v1.getOrElse(throw new IllegalStateException(s"Cannot widen with freed variable $v1"))
        val v2Intern = new Texpr1Intern(state.getEnvironment, v2.node)
        val assigned = state.assignCopy(apronManager, av1, v2Intern, null)
        state.join(apronManager, assigned)
        Unchanged(Val.Double(v1))
      }
    case (v1, v2) => Widen(v1.asV, v2.asV).map(Val.Other.apply)
  }


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
    setVars(vars)
    try f finally {
      this._data = snapData
      this.names = snapNames
      this.allocatedVars.foreach(apron.freeVariable)
      this.vars = snapVars
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
              apron.assign(av, exp).foreach(av2 => vars(ix) = Val.Int(av2))
            case _ =>
              val intVar = apron.addIntVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$name"))
              val maybeVar = apron.assign(intVar, exp)
              maybeVar.foreach(_ => assert(false))
              vars(ix) = Val.Int(intVar)
        case None => getDoubleVal(v) match
          case Some(exp) =>
            oldVal match
              case Val.Double(av) =>
                apron.assign(av, exp).foreach(av2 => vars(ix) = Val.Double(av2))
              case _ =>
                val doubleVar = apron.addDoubleVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$name"))
                apron.assign(doubleVar, exp).foreach(_ => assert(false))
                vars(ix) = Val.Double(doubleVar)
          case None =>
            vars(ix) = Val.Other(v)
      JOptionC.some(())
    } else {
      JOptionC.none
    }

  override def setLocal(ix: Int, v: V): JOptionC[Unit] =
    setLocal(ix, names.find(_._2 == ix).get._1, v)

  override def setLocalByName(x: Var, v: V): JOptionC[Unit] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => setLocal(ix, x, v)

  type State = (apron.ApronState, List[Val])

  /** state contains the constraints for the current frame only */
  override def getState: State = (apron.getState, vars.toList)

  private def setVarsConsistentWithState(vars: Array[Val]): Unit =
    this.vars = vars.map {
      case null => null
      case v@Val.Int(av) => if (av.isFree) Val.Int(apron.alloc.freshReference(av)) else v
      case v@Val.Double(av) => if (av.isFree) Val.Double(apron.alloc.freshReference(av)) else v
      case v@Val.Other(_) => v
    }

  override def setState(st: State): Unit =
    val state = apron.apronState
    for (v <- vars; av <- Val.getVar(v))
      av.free(state)
    apron.setState(st._1)
    setVarsConsistentWithState(st._2.toArray)
    println(s"Restored ApronCallFrame state $vars in $apron")

  override def join: Join[State] = {
    case ((s1, vars1), (s2, vars2)) =>
      val MaybeChanged(as, changed) = apron.join(s1, s2)
      val state = new Abstract1(apronManager, as.s)
      val MaybeChanged(vars, varsChanged) = CombineEquiList(using joinVal(state))(vars1, vars2)
      val newApronState = new apron.ApronState(state)
      if (Apron.debugJoinWiden)
        println(
          s"""Joining call frame
             |  vars1 = $vars1
             |  vars2 = $vars2
             |  vars = $vars
             |  changed = ${changed || varsChanged}""".stripMargin)
      MaybeChanged((newApronState, vars), changed || varsChanged || !as.s.isEqual(apronManager, state))
  }

  override def widen: Widen[State] = { case ((s1, vars1), (s2, vars2)) =>
    val MaybeChanged(as, changed) = apron.widen(s1, s2)
    val state = new Abstract1(apronManager, as.s)
    val MaybeChanged(vars, varsChanged) = CombineEquiList(using widenVal(state))(vars1, vars2)
    val newApronState = new apron.ApronState(state)
    if (Apron.debugJoinWiden)
      println(
        s"""Widening call frame
           |  vars1 = $vars1
           |  vars2 = $vars2
           |  vars = $vars
           |  changed = ${changed || varsChanged}""".stripMargin)
    MaybeChanged((newApronState, vars), changed || varsChanged || !as.s.isEqual(apronManager, state))
  }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new CallFrameJoiner[A])

  protected class CallFrameJoiner[A] extends apron.ApronComputationJoiner[A] {
    private val snapshot = vars.toList
    private var fVars: Array[Val] = _

    override def inbetween(): Unit =
      super.inbetween()
      fVars = vars
      setVarsConsistentWithState(snapshot.toArray)

    override def retainNone(): Unit =
      super.retainNone()
      setVarsConsistentWithState(snapshot.toArray)

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      super.retainFirst(fRes)
      setVarsConsistentWithState(fVars)

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      super.retainSecond(gRes)

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      super.retainBoth(fRes, gRes)
      if (vars.length != fVars.length)
        throw IllegalStateException()
      val join = joinVal(apron.getState.s)
      val joinedVars = vars.zip(fVars).map(join(_, _).get)

      if (Apron.debugJoinWiden)
        println(
          s"""Computation joiner call frame
           |  vars1 = ${fVars.toList}
           |  vars2 = ${vars.toList}
           |  vars = ${joinedVars.toList}""".stripMargin)

      setVarsConsistentWithState(joinedVars)
  }

