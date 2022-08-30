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

    def asV: V = this match
      case Int(v) => makeIntVal(Texpr1VarNode(v))
      case Double(v) => makeIntVal(Texpr1VarNode(v))
      case Other(v) => v
  object Val:
    def from(v: V): Val =
      getIntVal(v) match
        case Some(exp) =>
          val av = addIntVariable("foo")
          val expIntern = new Texpr1Intern(apronEnv, exp)
          apronState.assign(apronManager, av, expIntern, null)
          Val.Int(av)
        case _ => getDoubleVal(v) match
          case Some(exp) =>
            val av = addDoubleVariable("foo")
            val expIntern = new Texpr1Intern(apronEnv, exp)
            apronState.assign(apronManager, av, expIntern, null)
            Val.Double(av)
          case _ => Other(v)

  given Join[Val] = {
    case (Val.Int(v1), Val.Int(v2)) if v1 == v2 => MaybeChanged.Unchanged(Val.Int(v1))
    case (Val.Double(v1), Val.Double(v2)) if v1 == v2 => MaybeChanged.Unchanged(Val.Double(v1))
    case (v1, v2) => Join(v1.asV, v2.asV).map(Val.from)
  }

  given Widen[Val] = {
    case (Val.Int(v1), Val.Int(v2)) if v1 == v2 => MaybeChanged.Unchanged(Val.Int(v1))
    case (Val.Double(v1), Val.Double(v2)) if v1 == v2 => MaybeChanged.Unchanged(Val.Double(v1))
    case (v1, v2) => Widen(v1.asV, v2.asV).map(Val.from)
  }

  private var boundVars: Map[Int, Val] = Map()

  override def setVars(newVars: Iterable[(Var, V)]): Unit = {
    names = newVars.zipWithIndex.map(t => t._1._1 -> t._2).toMap

    var newBoundVars: Map[Int, Val] = Map()
    val newApronAssignments: ListBuffer[(ApronVar, Texpr1Node)] = ListBuffer()

    newVars.zipWithIndex.foreach { case ((x, v), ix) =>
      getIntVal(v) match
        case Some(exp) =>
          val av = addIntVariable(x.toString)
          newApronAssignments += av -> exp
          newBoundVars += ix -> Val.Int(av)
        case None => getDoubleVal(v) match
          case Some(exp) =>
            val av = addDoubleVariable(x.toString)
            newApronAssignments += av -> exp
            newBoundVars += ix -> Val.Double(av)
          case None =>
            newBoundVars += ix -> Val.Other(v)
    }
    boundVars = newBoundVars
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
          case _ =>
            val intVar = apron.addIntVariable(x.toString)
            apronState.assign(apronManager, intVar, vIntern, null)
            boundVars += x -> Val.Int(intVar)
      case None => getDoubleVal(v) match
        case Some(exp) =>
          val vIntern = new Texpr1Intern(apronEnv, exp)
          oldVal match
            case Val.Double(av) =>
              apronState.assign(apronManager, av, vIntern, null)
            case _ =>
              val doubleVar = apron.addDoubleVariable(x.toString)
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
    // TODO do we loose too much precision here?
    //    joinWith(st._1, false)
//    println(s"Old state $apronState")
//    println(s"New state ${st._1}")
    setLeastExtendingEnvironment(st._1)
    apronState = st._1
    this.boundVars = st._2

  override def join: Join[State] = (s1, s2) => {
    val lce = s1._1.getEnvironment.lce(s2._1.getEnvironment)
    val state1 = s1._1.changeEnvironmentCopy(apronManager, lce, false)
    val state2 = s2._1.changeEnvironmentCopy(apronManager, lce, false)
    val joined = state1.joinCopy(apronManager, state2)
    val MaybeChanged(joinedBoundVars, changedBoundVars) = Join(s1._2, s2._2)
    val changed = changedBoundVars || !joined.isEqual(apronManager, state1)
    MaybeChanged((joined, joinedBoundVars), changed)
  }
  override def widen: Widen[State] = (s1, s2) => {
    val lce = s1._1.getEnvironment.lce(s2._1.getEnvironment)
    val state1 = s1._1.changeEnvironmentCopy(apronManager, lce, false)
    val state2 = s2._1.changeEnvironmentCopy(apronManager, lce, false)
    val widened = state1.widening(apronManager, state2)
    given Finite[Int] with {}
    val MaybeChanged(widenedBoundVars, changedBoundVars) = Widen(s1._2, s2._2)
    val changed = changedBoundVars || !widened.isEqual(apronManager, state1)
    MaybeChanged((state1, widenedBoundVars), changed)
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

      // Least Environment Extending the two resulting environments
      // TODO CHECK
      setLeastExtendingEnvironment(fState)
      fState.join(apronManager, apronState)
      apronState = fState
  })