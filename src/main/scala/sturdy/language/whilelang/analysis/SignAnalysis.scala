package sturdy.language.whilelang.analysis

import sturdy.effect.AnalysisState
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.environment.AEnvironmentDynamicScope
import sturdy.effect.store.{AStoreMultiAddrThreadded, ManageableAddr}
import sturdy.effect.failure.{Failure, AFailureCollect}
import sturdy.fix
import sturdy.fix.given
import sturdy.language.whilelang.{GenericInterpreter, Statement, ConcreteInterpreter}
import sturdy.util.{Label, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{_, given}
import sturdy.values.doubles.{_, given}
import sturdy.values.references.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.{Topped, given}
import sturdy.values.Topped.{_, given}

import GenericInterpreter.GenericPhi

object SignAnalysis:
  enum Value:
    case BooleanValue(b: Topped[Boolean])
    case DoubleValue(d: DoubleSign)

    def asBoolean: Topped[Boolean] = this match {
      case BooleanValue(b) => b
      case _ => throw new IllegalArgumentException(s"Expected Boolean but got $this")
    }
    def asDouble: DoubleSign = this match {
      case DoubleValue(d) => d
      case _ => throw new IllegalArgumentException(s"Expected Double but got $this")
    }
  import Value._

  given Finite[Value] with {}

  given JoinValue[Value] with
    override def joinValues(v1: Value, v2: Value): Value = (v1, v2) match
      case (BooleanValue(b1), BooleanValue(b2)) => BooleanValue(flatToppedJoin.joinValues(b1, b2))
      case (DoubleValue(d1), DoubleValue(d2)) => DoubleValue(DoubleSignJoin.joinValues(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")

  type Context = Statement.Assign
  type Addr = AllocationSiteAddr
  type PowAddr = Powerset[Addr]
  type Environment =  Map[String, MayMust[PowAddr]]
  given Finite[String] with {}
  type Store = Map[Addr, Value]
  class Effects(initEnvironment: Environment, initStore: Store)
    extends ABoolBranching[Value]
    with AEnvironmentDynamicScope[String, PowAddr](initEnvironment)
    with AStoreMultiAddrThreadded[Addr, Value](initStore)
    with AAllocationFromContext[Context, PowAddr](a => Powerset(AllocationSiteAddr.Alloc(a.label)(true)))
    with AFailureCollect
    with AnalysisState[(Environment, Store), (Environment, Store)] {

    override def getInState(): (Environment, Store) = (getEnv, getStore)
    override def setInState(in: (Environment, Store)): Unit =
      setEnv(in._1)
      setStore(in._2)
    override def getOutState(): (Environment, Store) = (getEnv, getStore)
    override def setOutState(out: (Environment, Store)): Unit =
      setEnv(out._1)
      setStore(out._2)
    override def isOutStateStable(old: (Environment, Store), now: (Environment, Store)): Boolean = old == now
  }

  def apply(initEnvironment: Environment, initStore: Store): SignAnalysis = {
    val effects = new Effects(initEnvironment, initStore)

    given BooleanOps[Value] = new LiftedBooleanOps[Value, Topped[Boolean]](_.asBoolean, BooleanValue.apply)
    given DoubleOps[Value] = new LiftedDoubleOps[Value, DoubleSign](_.asDouble, DoubleValue.apply)
    given CompareOps[Value, Value]  = new LiftedCompareOps[Value, Value, DoubleSign, Topped[Boolean]](_.asDouble, BooleanValue.apply)
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = BooleanValue((v1, v2) match
        case (BooleanValue(b1), BooleanValue(b2)) => EqOps.equ(b1, b2)
        case (DoubleValue(d1), DoubleValue(d2)) => EqOps.equ(d1, d2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      )
      def neq(v1: Value, v2: Value): Value = BooleanValue((v1, v2) match
        case (BooleanValue(b1), BooleanValue(b2)) => EqOps.neq(b1, b2)
        case (DoubleValue(d1), DoubleValue(d2)) => EqOps.neq(d1, d2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    )

    new SignAnalysis(using effects) {}
  }

import SignAnalysis.{*, given}

class SignAnalysis
  (using effectOps: Effects)
  (using boolOps: BooleanOps[Value], doubleOps: DoubleOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value])
  extends GenericInterpreter[Value, PowAddr, Effects]:

  val isWhile: Statement => Boolean = {
    case Statement.While(_, _) => true
    case _ => false
  }

  val phi = fix.filter(isWhile,
    fix.iter.innermost(fix.ContextSensitive.none)
  )
  
