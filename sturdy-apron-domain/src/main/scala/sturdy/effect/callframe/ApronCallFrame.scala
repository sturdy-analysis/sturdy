package sturdy.effect.callframe

import apron.{Environment, Interval, Texpr1VarNode, Tcons1, StringVar, Manager, Abstract1}
import org.eclipse.collections.api.factory.BiMaps
import org.eclipse.collections.api.bimap.{ImmutableBiMap, MutableBiMap, BiMap}
import sturdy.apron.Apron
import sturdy.apron.ApronAllocationSite
import sturdy.apron.ApronExpr
import sturdy.data.{*, given}
import sturdy.data.MayJoin.WithJoin
import sturdy.effect.ComputationJoiner
import sturdy.effect.SturdyFailure
import sturdy.effect.TrySturdy
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.MaybeChanged.Unchanged
import sturdy.values.{Widen, Join}

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
                                   initVars: Iterable[(Var, V)] = Iterable.empty)
                                  (using site: CallFrameSite[Data])
                                  (using Join[V], Widen[V], ClassTag[V])
  extends DecidableMutableCallFrame[Data, Var, V](initData, initVars) :

  import apron.*

  enum Val:
    case Int(v: alloc.Var)
    case Double(v: alloc.Var)
    case Other(v: V)

    def asV: V = this match
      case Int(v) => makeIntVal(ApronExpr.Var(v))
      case Double(v) => makeIntVal(ApronExpr.Var(v))
      case Other(v) => v

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

  given Join[Val] = {
    case (Val.Int(v1), Val.Int(v2)) =>
      val joined = apron.joinValues(ApronExpr.Var(v1), ApronExpr.Var(v2), widen = false)
      joined.map { exp =>
        apron.assignStrong(v1, exp)
        Val.Int(v1)
      }
    case (Val.Double(v1), Val.Double(v2)) =>
      val joined = apron.joinValues(ApronExpr.Var(v1), ApronExpr.Var(v2), widen = false)
      joined.map { exp =>
        apron.assignStrong(v1, exp)
        Val.Int(v1)
      }
    case (v1, v2) => Join(v1.asV, v2.asV).map(Val.Other.apply)
  }

  given Widen[Val] = {
    case (Val.Int(v1), Val.Int(v2)) =>
      val widened = apron.joinValues(ApronExpr.Var(v1), ApronExpr.Var(v2), widen = true)
      widened.map { exp =>
        apron.assignStrong(v1, exp)
        Val.Int(v1)
      }
    case (Val.Double(v1), Val.Double(v2)) =>
      val widened = apron.joinValues(ApronExpr.Var(v1), ApronExpr.Var(v2), widen = true)
      widened.map { exp =>
        apron.assignStrong(v1, exp)
        Val.Int(v1)
      }
    case (v1, v2) => Widen(v1.asV, v2.asV).map(Val.Other.apply)
  }

  private var allocatedVars: Set[alloc.Var] = Set()
  private var boundVars: Map[Int, Val] = Map()

  override def setVars(newVars: Iterable[(Var, V)]): Unit = {
    names = newVars.zipWithIndex.map(t => t._1._1 -> t._2).toMap

    var newBoundVars: Map[Int, Val] = Map()
    val newApronAssignments: ListBuffer[(alloc.Var, ApronExpr)] = ListBuffer()

    newVars.zipWithIndex.foreach { case ((x, v), ix) =>
      getIntVal(v) match
        case Some(exp) =>
          val av = addIntVariable(x.toString, ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
          allocatedVars += av
          newApronAssignments += av -> exp
          newBoundVars += ix -> Val.Int(av)
        case None => getDoubleVal(v) match
          case Some(exp) =>
            val av = addDoubleVariable(x.toString, ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
            allocatedVars += av
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
            val intVar = addIntVariable(x.toString, ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
            allocatedVars += intVar
            apron.assign(intVar, exp)
            boundVars += x -> Val.Int(intVar)
      case None => getDoubleVal(v) match
        case Some(exp) =>
          oldVal match
            case Val.Double(av) =>
              apron.assign(av, exp)
            case _ =>
              val doubleVar = addDoubleVariable(x.toString, ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
              allocatedVars += doubleVar
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
    apron.makeComputationJoiner
