package sturdy.effect.callframe

import apron.{Abstract1, Environment, Interval, Manager, StringVar, Tcons1, Texpr1Intern, Texpr1Node, Texpr1VarNode, Var as ApronVar}
import org.eclipse.collections.api.factory.BiMaps
import org.eclipse.collections.api.bimap.{BiMap, ImmutableBiMap, MutableBiMap}
import sturdy.apron.Apron
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.ComputationJoiner
import sturdy.effect.SturdyFailure
import sturdy.effect.TrySturdy
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.{Join, Widen}

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

class ApronCallFrame[Data, Var, V](apron: Apron,
                                            initData: Data,
                                            getIntVal: V => Option[Texpr1Node],
                                            getDoubleVal: V => Option[Texpr1Node],
                                            makeIntVal: Texpr1Node => V,
                                            makeDoubleVal: Texpr1Node => V,
                                            initVars: Iterable[(Var, V)] = Iterable.empty)
                                           (using Join[V], Widen[V], ClassTag[V])
  extends DecidableMutableCallFrame[Data, Var, V](initData, initVars) :
  //extends MutableCallFrame[Data, Var, V, NoJoin] with DecidableCallFrame[Data, Var, V]:

  import apron.*

  enum Val:
    case Int(v: ApronVar)
    case Double(v: ApronVar)
    case Other(v: V)

  given Join[Val] = {
    case (Val.Int(v1), Val.Int(v2)) if v1 == v2 => MaybeChanged.Unchanged(Val.Int(v1))
    case (Val.Double(v1), Val.Double(v2)) if v1 == v2 => MaybeChanged.Unchanged(Val.Double(v1))
    case (Val.Other(v1), Val.Other(v2)) => Join(v1, v2).map(Val.Other.apply)
    case (v1, v2) => throw new Exception(s"Cannot join $v1 and $v2")
  }

  given Widen[Val] = {
    case (Val.Int(v1), Val.Int(v2)) if v1 == v2 => MaybeChanged.Unchanged(Val.Int(v1))
    case (Val.Double(v1), Val.Double(v2)) if v1 == v2 => MaybeChanged.Unchanged(Val.Double(v1))
    case (Val.Other(v1), Val.Other(v2)) => Widen(v1, v2).map(Val.Other.apply)
    case (v1, v2) => throw new Exception(s"Cannot join $v1 and $v2")
  }

  private var boundVars: Map[Int, Val] = Map()

  override def setVars(newVars: Iterable[(Var, V)]): Unit = {
    names = newVars.zipWithIndex.map(t => t._1._1 -> t._2).toMap

    var newBoundVars: Map[Int, Val] = Map()
    val newApronIntVars: ListBuffer[ApronVar] = ListBuffer()
    val newApronDoubleVars: ListBuffer[ApronVar] = ListBuffer()
    val newApronAssignments: ListBuffer[(ApronVar, Texpr1Node)] = ListBuffer()

    newVars.zipWithIndex.foreach { case ((x, v), ix) =>
      getIntVal(v) match
        case Some(exp) =>
          val av = new StringVar(s"apronI_${apronVarCount}_$x")
          apronVarCount += 1
          newApronIntVars += av
          newApronAssignments += av -> exp
          newBoundVars += ix -> Val.Int(av)
        case None => getDoubleVal(v) match
          case Some(exp) =>
            val av = new StringVar(s"apronD_${apronVarCount}_$x")
            apronVarCount += 1
            newApronDoubleVars += av
            newApronAssignments += av -> exp
            newBoundVars += ix -> Val.Double(av)
          case None =>
            newBoundVars += ix -> Val.Other(v)
    }
    boundVars = newBoundVars
    apronEnv = apronEnv.add(newApronIntVars.toArray, Array.empty[ApronVar])
    apronState.changeEnvironment(apronManager, apronEnv, false)
    newApronAssignments.foreach { case (av, exp) =>
      val vIntern = new Texpr1Intern(apronEnv, exp)
      apronState.assign(apronManager, av, vIntern, null)
    }
  }

  setVars(initVars)

  override def withNew[A](d: Data, vars: Iterable[(Var, V)])(f: => A): A = {
    val snapData = this._data
    val snapNames = this.names
    val snapBoundVars = this.boundVars
    this._data = d
    setVars(vars)
    try f finally {
      this._data = snapData
      this.names = snapNames
      this.boundVars = snapBoundVars
    }
  }

  override def data: Data = _data

  override def getLocal(x: Int): JOptionC[V] = boundVars.get(x) match
    case None => JOptionC.none
    case Some(e) => e match
      case Val.Int(av) => JOptionC.some(makeIntVal(new Texpr1VarNode(av)))
      case Val.Double(av) => JOptionC.some(makeDoubleVal(new Texpr1VarNode(av)))
      case Val.Other(v) => JOptionC.some(v)

  override def getLocalByName(x: Var): JOptionC[V] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => getLocal(ix)

  override def setLocal(x: Int, v: V): JOptionC[Unit] = boundVars.get(x) match
    case None => JOptionC.none
    case Some(oldVal) => getIntVal(v) match
      case Some(exp) =>
        val vIntern = new Texpr1Intern(apronEnv, exp)
        oldVal match
          case Val.Int(av) =>
            apronState.assign(apronManager, av, vIntern, null)
          case Val.Double(av) =>
            val intVar = new StringVar("apronI" + av.toString.substring("apronD".length))
            if (!apronEnv.hasVar(intVar)) {
              apronEnv = apronEnv.add(Array[ApronVar](intVar), null)
              apronState.changeEnvironment(apronManager, apronEnv, false)
            }
            apronState.assign(apronManager, intVar, vIntern, null)
            boundVars += x -> Val.Int(intVar)
          case Val.Other(_) =>
            val intVar = new StringVar(s"apronI_${apronVarCount}_$x")
            apronVarCount += 1
            if (!apronEnv.hasVar(intVar)) {
              apronEnv = apronEnv.add(Array[ApronVar](intVar), null)
              apronState.changeEnvironment(apronManager, apronEnv, false)
            }
            apronState.assign(apronManager, intVar, vIntern, null)
            boundVars += x -> Val.Int(intVar)
      case None => getDoubleVal(v) match
        case Some(exp) =>
          val vIntern = new Texpr1Intern(apronEnv, exp)
          oldVal match
            case Val.Int(av) =>
              val doubleVar = new StringVar("apronD" + av.toString.substring("apronI".length))
              if (!apronEnv.hasVar(doubleVar)) {
                apronEnv = apronEnv.add(null, Array[ApronVar](doubleVar))
                apronState.changeEnvironment(apronManager, apronEnv, false)
              }
              apronState.assign(apronManager, doubleVar, vIntern, null)
              boundVars += x -> Val.Double(doubleVar)
            case Val.Double(av) =>
              apronState.assign(apronManager, av, vIntern, null)
            case Val.Other(_) =>
              val doubleVar = new StringVar(s"apronD_${apronVarCount}_$x")
              apronVarCount += 1
              if (!apronEnv.hasVar(doubleVar)) {
                apronEnv = apronEnv.add(null, Array[ApronVar](doubleVar))
                apronState.changeEnvironment(apronManager, apronEnv, false)
              }
              apronState.assign(apronManager, doubleVar, vIntern, null)
              boundVars += x -> Val.Double(doubleVar)
        case None =>
          boundVars += x -> Val.Other(v)
      JOptionC.some(())

  override def setLocalByName(x: Var, v: V): JOptionC[Unit] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => setLocal(ix, v)

  type State = (Abstract1, Map[Int, Val])
  /** state contains the constraints for the current frame only */
  override def getState: State =
    (new Abstract1(apronManager, apronState), boundVars)

  override def setState(st: State): Unit =
    apronState = st._1
    this.boundVars = st._2

  override def join: Join[State] = (s1, s2) => {
    val joinedState = s1._1.joinCopy(apronManager, s2._1)
    val MaybeChanged(joinedBoundVars, changedBoundVars) = Join(s1._2, s2._2)
    MaybeChanged((joinedState, joinedBoundVars), changedBoundVars || s1._1.isEqual(apronManager, joinedState))
  }
  override def widen: Widen[State] = (s1, s2) => {
    val widenedState = s1._1.widening(apronManager, s2._1)
    given Finite[Int] with {}
    val MaybeChanged(widenedBoundVars, changedBoundVars) = Widen(s1._2, s2._2)
    MaybeChanged((widenedState, widenedBoundVars), changedBoundVars || s1._1.isEqual(apronManager, widenedState))
  }

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new ComputationJoiner[A] {
    private val snapshot = new Abstract1(apronManager, apronState)
    private var fState: Abstract1 = _

    override def inbetween(): Unit =
      fState = apronState
      apronState = snapshot

    override def retainNone(): Unit =
      apronState = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      apronState = fState

    override def retainSecond(gRes: TrySturdy[A]): Unit = {}

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      fState.join(apronManager, apronState)
      apronState = fState
  })
