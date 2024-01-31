package sturdy.effect.callframe

import apron.*
import sturdy.apron.{ApronExpr, given}
import sturdy.data.{JOption, JOptionA, JOptionC, NoJoin, WithJoin}
import sturdy.effect.callframe.{ConcreteCallFrame, JoinableDecidableCallFrame, MutableCallFrame}
import sturdy.effect.store.{ApronStore, ClosedEquality, ClosedHashCode, RecencyStore, given}
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

trait HasContext[Context]:
  def currentContext: Context

object HasContext:
  def currentContext[Context](using getContext: HasContext[Context]): Context = getContext.currentContext

case class LocalVariableContext[Var,Ctx](variable: Var, ctx: Ctx)

given LocalVariableContextOrdering[Var: Ordering, Ctx: Ordering]: Ordering[LocalVariableContext[Var,Ctx]] =
  Ordering.by[LocalVariableContext[Var, Ctx], (Var,Ctx)](localVarCtx => (localVarCtx.variable, localVarCtx.ctx))

final class ApronCallFrame
  [Data,
   Var: Ordering,
   CallSite,
   Ctx: Ordering]
  (initData: Data,
   initVars: Iterable[(Var, Option[ApronExpr[VirtualAddress[LocalVariableContext[Var,Ctx]]]])],
   apronManager: Manager)
  (using
   Finite[LocalVariableContext[Var,Ctx]],
   HasContext[Ctx]
  )
  extends MutableCallFrame[Data, Var, ApronExpr[VirtualAddress[LocalVariableContext[Var,Ctx]]], CallSite, NoJoin]:

  final type Context = LocalVariableContext[Var,Ctx]
  final type VirtAddr = VirtualAddress[Context]
  final type PhysAddr = PhysicalAddress[Context]
  final type PowPhysAddr = PowersetAddr[PhysicalAddress[Context], PhysicalAddress[Context]]
  final type PowVirtAddr = PowVirtualAddress[Context]
  final type ApronExprVirtAddr = ApronExpr[VirtualAddress[Context]]
  final type ApronExprPhysAddr = ApronExpr[PhysicalAddress[Context]]

  val apronStore: ApronStore[Context, PhysAddr, PowPhysAddr, ApronExprPhysAddr] = ApronStore(
    apronManager,
    Abstract1(apronManager, new Environment()),
    getIntVal = Some[ApronExprPhysAddr](_),
    makeIntVal = (expr: ApronExprPhysAddr, state: Abstract1) =>
      ApronExpr.Constant(state.getBound(apronManager, expr.toIntern(state.getEnvironment)))
  )

  val addressTranslation: AddressTranslation[LocalVariableContext[Var, Ctx]] = AddressTranslation.empty

  val recencyStore: RecencyStore[Context, PowVirtAddr, ApronExprPhysAddr] =
    RecencyStore(
      apronStore,
      addressTranslation)

  val addressCallFrame: JoinableDecidableCallFrame[Data, Var, VirtAddr, CallSite] =
    JoinableDecidableCallFrame(
      initData,
      Iterable.empty
    )

  override def data: Data = addressCallFrame.data

  def getAddressTranslation: AddressTranslation[Context] = recencyStore.getAddressTranslation

  def setVars(newVars: Iterable[(Var, Option[ApronExprVirtAddr])]) =
    addressCallFrame.setVars(
      newVars.map((variable, _) =>
        val ctx = LocalVariableContext(variable, HasContext.currentContext)
        (variable, Some(recencyStore.alloc(ctx)))
      )
    )

    for((variable, exprOption) <- newVars;
        expr <- exprOption)
      setLocalByName(variable, expr)

  setVars(initVars)

  override def setLocal(idx: Int, exprVirtAddr: ApronExprVirtAddr): JOptionC[Unit] =
    addressCallFrame.getLocal(idx).map(virt =>
      recencyStore.write(PowVirtualAddress(virt), virtToPhys(exprVirtAddr))
    )

  override def setLocalByName(x: Var, expr: ApronExprVirtAddr): JOptionC[Unit] =
    addressCallFrame.getFrameNames.get(x) match
      case None => JOptionC.none
      case Some(idx) => setLocal(idx, expr)

  override def getLocal(x: Int): JOptionC[ApronExprVirtAddr] =
    addressCallFrame.getLocal(x).map(ApronExpr._var(_))

  override def getLocalByName(x: Var): JOptionC[ApronExprVirtAddr] =
    addressCallFrame.getLocalByName(x).map(ApronExpr._var(_))

  def getBound(x: ApronExprVirtAddr): apron.Interval =
    val apronState: Abstract1 = apronStore.getState
    val env = apronState.getEnvironment
    apronState.getBound(apronManager, virtToPhys(x).toIntern(env))

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[ApronExprVirtAddr])], site: CallSite)(f: => A): A =
    val virtAddrs = vars.map((variable, _) =>
      val ctx = LocalVariableContext(variable, HasContext.currentContext)
      (variable, Some(recencyStore.alloc(ctx)))
    )
    addressCallFrame.withNew(d, virtAddrs, site) {
      for ((variable, exprOption) <- vars; expr <- exprOption)
        setLocalByName(variable, expr)
      f
    }

  private def virtToPhys(exprVirtAddr: ApronExprVirtAddr): ApronExprPhysAddr =
    // To convert ApronExprVirtAddr to ApronExprPhysAddr, we need to combine virtual addresses
    // that map to two physical addresses {(ctx,recent),(ctx,old)}.
    // Specifically, we join (ctx,recent) into (ctx,old), such that the virtual address
    // can be mapped to {(ctx,old)}.
    val virtRecentOlds = PowVirtualAddress(exprVirtAddr.vars.filter(virt => virt.physical.addrs.size == 2))
    recencyStore.joinRecentIntoOld(virtRecentOlds)
    exprVirtAddr.mapAddr(
      virt =>
        val physicals = virt.physical
        if (physicals.addrs.size == 1)
          physicals.iterator.next()
        else
          throw IllegalStateException(s"${virt} did not map to a single physical address, but to ${physicals}")
    )

  case class ApronCallFrameState(recencyStoreState: recencyStore.State, addressCallFrameState: addressCallFrame.State):
    override def equals(obj: Any): Boolean = throw new UnsupportedOperationException("Use ApronCallFrame.closedEquality")
    override def hashCode(): Int = throw new UnsupportedOperationException("Use ApronCallFrame.closedEquality")


  override type State = ApronCallFrameState

  override def getState: ApronCallFrameState =
    ApronCallFrameState(recencyStore.getState, addressCallFrame.getState)

  override def setState(state: ApronCallFrameState): Unit =
    recencyStore.setState(state.recencyStoreState)
    addressCallFrame.setState(state.addressCallFrameState)

  override def join: Join[ApronCallFrameState] =
    (v1: ApronCallFrameState, v2: ApronCallFrameState) =>
      val joinedRecencyStoreState = recencyStore.join(v1.recencyStoreState, v2.recencyStoreState)
      val joinedAddressCallFrameState = addressCallFrame.join(v1.addressCallFrameState, v2.addressCallFrameState)
      val joinedApronCallFrameState = ApronCallFrameState(joinedRecencyStoreState.get, joinedAddressCallFrameState.get)
      MaybeChanged(joinedApronCallFrameState,  joinedRecencyStoreState.hasChanged || joinedAddressCallFrameState.hasChanged)

  override def widen: Widen[ApronCallFrameState] =
    (v1: ApronCallFrameState, v2: ApronCallFrameState) =>
      val widenedRecencyStoreState = recencyStore.widen(v1.recencyStoreState, v2.recencyStoreState)
      val widenedAddressCallFrameState = addressCallFrame.widen(v1.addressCallFrameState, v2.addressCallFrameState)
      val widenedApronCallFrameState = ApronCallFrameState(widenedRecencyStoreState.get, widenedAddressCallFrameState.get)
      MaybeChanged(widenedApronCallFrameState, widenedRecencyStoreState.hasChanged || widenedAddressCallFrameState.hasChanged)

  given VirtAddrJoin: Join[VirtAddr] with
    override def apply(v1: VirtAddr, v2: VirtAddr): MaybeChanged[VirtAddr] =
      if(v1.physical == v2.physical)
        MaybeChanged(v1, false)
      else
        throw new IllegalStateException(s"Cannot join ${v1} and ${v2}")

//  def closedEquality: ClosedEquality[addressTranslation.State, ApronCallFrameState] =
//    new ClosedEquality[addressTranslation.State, ApronCallFrameState]:
//      val recencyStoreEquals = recencyStore.closedEquality
//
//      override def closedEquals(closure1: addressTranslation.State, state1: ApronCallFrameState, closure2: addressTranslation.State, state2: ApronCallFrameState): Boolean =
//        recencyStoreEquals.closedEquals(closure1, state1.recencyStoreState, closure2, state2.recencyStoreState) &&
//          listClosedEquality[addressTranslation.State, VirtAddr](using VirtAddrClosedEquality[Context]).closedEquals(closure1, state1.addressCallFrameState, closure2, state2.addressCallFrameState)
//
//      override def closedHashCode(closure: addressTranslation.State, state: ApronCallFrameState): Int =
//        (recencyStoreEquals.closedHashCode(closure, state.recencyStoreState),
//          listClosedEquality[addressTranslation.State, VirtAddr].closedHashCode(closure, state.addressCallFrameState)).hashCode()


/* package sturdy.effect.callframe

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

class ApronCallFrame[Data, Var, V, Site](val apron: Apron,
                                   initData: Data,
                                   getIntVal: V => Option[ApronExpr],
                                   getDoubleVal: V => Option[ApronExpr],
                                   makeIntVal: ApronExpr => V,
                                   makeDoubleVal: ApronExpr => V,
                                   initVars: Iterable[(Var, Option[V])] = Iterable.empty)
                                  (using site: CallFrameSite[Data])
                                  (using Join[V], Widen[V], ClassTag[V])
  extends MutableCallFrame[Data, Var, V, Site, NoJoin], DecidableCallFrame[Data, Var, V, Site]:

  val vals: ApronVal[V] = new ApronVal(apron, makeIntVal, makeDoubleVal)

  import vals.Val
  import apron.{apronManager, alloc}

  protected var _data: Data = initData
  protected var vars: Array[Val] = _
  protected var names: Map[Var, Int] = _

  setVars(initVars)

  override def toString: String =
    s"CallFrame(${vars.mkString(", ")}: $apron)"

  private var additionalVars: Map[String, ApronVar] = Map()
  private def activeVars: Iterable[ApronVar] = additionalVars.values ++ vars.iterator.flatMap(Val.getVar)

//    override def equals(obj: Any): Boolean = (this, obj) match
//      case (Int(v1), Int(v2)) => apron.currentScope.getBound(v1) == apron.currentScope.getBound(v2)
//      case (Double(v1), Double(v2)) => apron.currentScope.getBound(v1) == apron.currentScope.getBound(v2)
//      case (Other(v1), Other(v2)) => v1 == v2
//      case _ => false
//
//    override def hashCode: scala.Int = this match
//      case Int(v) => apron.currentScope.getBound(v).hashCode
//      case Double(v) => apron.currentScope.getBound(v).hashCode
//      case Other(v) => v.hashCode

  def setVars(newVars: Iterable[(Var, Option[V])]): Unit = {
    names = newVars.zipWithIndex.map(t => t._1._1 -> t._2).toMap

    vars = Array.ofDim(newVars.size)
    val argVarAssignments: ListBuffer[(alloc.Var, ApronExpr)] = ListBuffer()
    val localAssignments: ListBuffer[(alloc.Var, ApronExpr)] = ListBuffer()

    newVars.zipWithIndex.foreach {
      case ((_, None), ix) =>
        vars(ix) = null
      case ((x, Some(v)), ix) =>
        getIntVal(v) match
          case Some(exp) =>
//            val argV = apron.addIntVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:arg_$ix"))
            val av = apron.addIntVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
//            argVarAssignments += argV -> exp
//            localAssignments += av -> ApronExpr.Var(argV)
            localAssignments += av -> exp
            vars(ix) = Val.Int(av)
          case None => getDoubleVal(v) match
            case Some(exp) =>
//              val argV = apron.addIntVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:arg_$ix"))
              val av = apron.addDoubleVariable(ApronAllocationSite.LocalVar(s"${site(_data)}:$x"))
//              argVarAssignments += argV -> exp
//              localAssignments += av -> ApronExpr.Var(argV)
              localAssignments += av -> exp
              vars(ix) = Val.Double(av)
            case None =>
              vars(ix) = Val.Other(v)
    }

    argVarAssignments.foreach { case (av, exp) => apron.assign(av, exp) }
    localAssignments.foreach { case (av, exp) => apron.assign(av, exp) }
  }

  override def withNew[A](d: Data, vars: Iterable[(Var, Option[V])], site: Site)(f: => A): A = {
    val snapData = this._data
    val snapNames = this.names
    val snapVars = this.vars.toList
    val snapAdditionalVars = this.additionalVars
    val snapState = apron.getState

    if (Apron.debugScope)
      println(s"Before frame: $this")

    this._data = d
    this.additionalVars = Map()
    setVars(vars)

    val fres = util.Try(f)
    if (Apron.debugScope)
      println(s"End of frame: $this <- $fres")

    // introduce apron variable for function result to retain relation on function inputs.
    val fresVarSite = ApronAllocationSite.CallReturnVar(site)
    var newVar: Option[ApronVar] = None
    val fresUpdated: util.Try[A] = fres match
      case util.Success(v: V) => getIntVal(v) match
        case Some(exp) =>
          val av = apron.addIntVariable(fresVarSite)
          newVar = Some(av)
          apron.assign(av, exp)
          println(s"Introducing additional var $av = $exp")
          util.Success(makeIntVal(ApronExpr.Var(av)).asInstanceOf[A])
        case None => getDoubleVal(v) match
          case Some(exp) =>
            val av = apron.addDoubleVariable(fresVarSite)
            newVar = Some(av)
            apron.assign(av, exp)
            util.Success(makeDoubleVal(ApronExpr.Var(av)).asInstanceOf[A])
          case None => fres
      case _ => fres

    // free active variables
    val oldActiveVars = activeVars
    val newAdditionalVars = snapAdditionalVars ++ newVar.map(v => v.av.toString -> v)

    oldActiveVars.foreach(v => apron.freeVariable(v.asInstanceOf[alloc.Var]))
    if (Apron.debugScope)
      println(s"After free:   $this <- $fresUpdated")

    this._data = snapData
    this.names = snapNames
    this.vars = snapVars.map { v => Val.getVar(v) match
      case None => v
      case Some(av) =>
        val freed = apron.currentScope.getFreedReference(av).orElse {
          if (snapState.isBound(av) && !apron.currentScope.isBound(av))
            Some(ApronExpr.num(snapState.getBound(av)))
          else
            None
        }
        // restore shadowed/forgotten variables from before
        if (freed.isDefined) {
          val av2 = apron.alloc.freshReference(av.asInstanceOf[apron.alloc.Var])
          apron.assign(av2, freed.get)
          v.replaceVar(av2)
        }
        else
          v
    }.toArray
    println(s"Replace addtional vars $additionalVars with ${(snapAdditionalVars ++ newVar.map(v => v.av.toString -> v)).values}")
    this.additionalVars = newAdditionalVars
    if (Apron.debugScope)
      println(s"After frame:  $this")

    fresUpdated.get
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
    // apron => ApronStore on top of RecencyStore,
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

  private def setVarsConsistentWithState(vars: Array[Val]): Unit =
    this.vars = vars.map {
      case null => null
      case Val.Int(av) if apron.currentScope.isFreed(av) => Val.Int(apron.alloc.freshReference(av.asInstanceOf[apron.alloc.Var]))
      case Val.Double(av) if apron.currentScope.isFreed(av) => Val.Double(apron.alloc.freshReference(av.asInstanceOf[apron.alloc.Var]))
      case v => v
    }

  type State = ApronCallFrameState

  override def getState: State =
    new ApronCallFrameState(apron.getState, vars.toList, additionalVars)

  override def setState(st: State): Unit =
//    if (Apron.debugAlloc)
//      println(apron.alloc)
    if (Apron.debugJoinWiden || Apron.debugAlloc)
      println(s"Restoring ApronCallFrame from ${vars.toList} in $apron with ${apron.currentScope}")
    apron.setState(st.as)
    setVarsConsistentWithState(st.vars.toArray)
    println(s"Replace addtional vars $additionalVars with ${st.additionalVars.values}")
    this.additionalVars = st.additionalVars
    if (Apron.debugJoinWiden || Apron.debugAlloc)
      println(s"Restoring ApronCallFrame to   ${vars.toList} in $apron with ${apron.currentScope}")
//    if (Apron.debugAlloc)
//      println(apron.alloc)

  def combine(st1: State, st2: State, widen: Boolean): MaybeChanged[State] =
    val MaybeChanged(state, apronChanged) = if (widen) apron.widen(st1.as, st2.as) else apron.join(st1.as, st2.as)
    val MaybeChanged((vars, combinedState1), varsChanged) = apron.joins.combineValLists(vals)(state, st1.vars, st2.vars, widen)
    val MaybeChanged((joinedAdditionalVars, combinedState2), additionalVarsChanged) = apron.joins.combineAdditionalVars(combinedState1, st1.additionalVars, st2.additionalVars, widen)

    if (Apron.debugJoinWiden)
      println(
        s"""${if (widen) "Widening" else "Joining"} apron call frame
           |  vars1 = ${st1.vars}
           |  vars2 = ${st2.vars}
           |  vars  = $vars
           |  apron = $combinedState2
           |  apronChanged = $apronChanged
           |  varsChanged = $varsChanged""".stripMargin)

    MaybeChanged(new ApronCallFrameState(combinedState2, vars, joinedAdditionalVars), apronChanged || varsChanged)

  override def join: Join[State] = combine(_, _, widen = false)

  override def widen: Widen[State] = combine(_, _, widen = true)

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new CallFrameJoiner[A])

  protected class CallFrameJoiner[A] extends apron.ApronComputationJoiner[A] {
    private val snapshot = vars.toList
    private val snapAdditionalVars = additionalVars
    private var fVars: Array[Val] = _
    private var fAdditionalVars: Map[String, ApronVar] = _

    override def inbetween(): Unit =
      super.inbetween()
      fVars = vars
      fAdditionalVars = additionalVars
      val snapshotWithNewVars = snapshot.zip(fVars).map { case (null, v: (Val.Int | Val.Double)) => v; case (v, _) => v }
      setVarsConsistentWithState(snapshotWithNewVars.toArray)
      additionalVars = snapAdditionalVars

    override def retainNone(): Unit =
      super.retainNone()
      setVarsConsistentWithState(snapshot.toArray)
      additionalVars = snapAdditionalVars

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      super.retainFirst(fRes)
      setVarsConsistentWithState(fVars)
      additionalVars = fAdditionalVars

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      super.retainSecond(gRes)

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      if (vars.length != fVars.length)
        throw IllegalStateException()

      super.retainBoth(fRes, gRes)

      val fVarsStr = if (Apron.debugJoinWiden) fVars.toList.toString else ""
      val varsStr = if (Apron.debugJoinWiden) vars.toList.toString else ""

      val MaybeChanged((joinedVars, st1), _) = apron.joins.combineValLists(vals)(apron.getState, fVars.toList, vars.toList, widen = false)
      val MaybeChanged((joinedAdditionalVars, st2), _) = apron.joins.combineAdditionalVars(st1, fAdditionalVars, additionalVars, widen = false)
      apron.setState(st2)
      additionalVars = joinedAdditionalVars

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

  class ApronCallFrameState(val as: ApronState, val vars: List[vals.Val], val additionalVars: Map[String, ApronVar]):
    import vals.*
    override def equals(obj: Any): Boolean = obj match
      case that: ApronCallFrameState =>
        this.vars.size == that.vars.size &&
        this.as == that.as &&
        this.vars.zip(that.vars).forall((v1,v2) => if (v1 == null) v2 == null else v1.isEqual(v2, as))
      case _ =>
        false

    override def hashCode(): Int =
      as.hashCode + vars.map(v => if (v == null) -1 else v.hashCode(as)).hashCode()

    override def toString: String =
      s"CallFrame($vars, $as)"
*/


