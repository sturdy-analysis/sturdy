package sturdy.effect.callframe

import apron.{Abstract1, Environment, Interval, Manager, StringVar, Tcons1, Texpr1Node, Texpr1VarNode, Var as ApronVar}
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
          apron.assign(av, exp)
          Val.Int(av)
        case _ => getDoubleVal(v) match
          case Some(exp) =>
            val av = addDoubleVariable("foo")
            assign(av, exp)
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
    newApronAssignments.foreach { case (av, exp) => assign(av, exp) }
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
        oldVal match
          case Val.Int(av) =>
            apron.assign(av, exp)
          case _ =>
            val intVar = addIntVariable(x.toString)
            apron.assign(intVar, exp)
            boundVars += x -> Val.Int(intVar)
      case None => getDoubleVal(v) match
        case Some(exp) =>
          oldVal match
            case Val.Double(av) =>
              apron.assign(av, exp)
            case _ =>
              val doubleVar = addDoubleVariable(x.toString)
              apron.assign(doubleVar, exp)
              boundVars += x -> Val.Double(doubleVar)
        case None =>
          boundVars += x -> Val.Other(v)
      JOptionC.some(())

  override def setLocalByName(x: Var, v: V): JOptionC[Unit] = names.get(x) match
    case None => JOptionC.none
    case Some(ix) => setLocal(ix, v)

  type State = (apron.State, Map[Int, Val])
  /** state contains the constraints for the current frame only */
  override def getState: State = (apron.getState, boundVars)

  override def setState(st: State): Unit =
    apron.setState(st._1)
    this.boundVars = st._2

  override def join: Join[State] =
    implicit val apronJoin: Join[apron.State] = apron.join
    implicitly

  override def widen: Widen[State] =
    implicit val apronWiden: Widen[apron.State] = apron.widen
    // TODO better represent bound vars as array and list like in the regular call frame
    given Finite[Int] with {}
    implicitly

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] =
    Some(apron.makeComputationJoiner)
