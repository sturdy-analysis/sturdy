package sturdy.language.whilelang.analysis

import sturdy.effect.JoinComputation
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.environment.AEnvironmentDynamicScope
import sturdy.effect.store.{AStoreMultiAddrThreadded, ManageableAddr}
import sturdy.effect.failure.{Failure, AFailureCollect}
import sturdy.fix.CFixpoint
import sturdy.language.whilelang.GenericInterpreter
import sturdy.language.whilelang.Statement
import sturdy.util.{Label, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{_, given}
import sturdy.values.doubles.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.{Topped, given}
import sturdy.values.Topped.{_, given}

object IntervalAnalysis:
  enum Value:
    case BooleanValue(b: Topped[Boolean])
    case DoubleValue(d: Topped[DoubleInterval])

    def asBoolean: Topped[Boolean] = this match
      case BooleanValue(b) => b
      case _ => throw new IllegalArgumentException(s"Expected Boolean but got $this")
    def asDouble: Topped[DoubleInterval] = this match
      case DoubleValue(d) => d
      case _ => throw new IllegalArgumentException(s"Expected Double but got $this")

  import Value._

  given JoinValue[Value] with
    override def joinValues(v1: Value, v2: Value): Value = (v1, v2) match
      case (BooleanValue(b1), BooleanValue(b2)) => BooleanValue(flatToppedJoin.joinValues(b1, b2))
      case (DoubleValue(d1), DoubleValue(d2)) => DoubleValue(nestedToppedJoin[DoubleInterval].joinValues(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")


  type Context = Statement.Assign
  case class Addr(l: Label) extends ManageableAddr(false)
  given Structural[Addr] with {}
  type PowAddr = Powerset[Addr]
  type Environment =  Map[String, (Boolean, PowAddr)]
  type Store = Map[Addr, Value]
  class Effects(initEnvironment: Environment, initStore: Store)
    extends ABoolBranching[Value]
    with AEnvironmentDynamicScope[String, PowAddr](initEnvironment)
    with AStoreMultiAddrThreadded[Addr, Value](initStore)
    with AAllocationFromContext[Context, PowAddr](a => Powerset(Addr(a.label)))
    with AFailureCollect
  type Fix = CFixpoint[Statement, Unit]

  def apply(initEnvironment: Environment, initStore: Store) = {
    val effects = new Effects(initEnvironment, initStore)
    val fixpoint = new CFixpoint[Statement, Unit]

    given BooleanOps[Value] = new LiftedBooleanOps[Value, Topped[Boolean]](_.asBoolean, BooleanValue.apply)
    given DoubleOps[Value] = new LiftedDoubleOps[Value, Topped[DoubleInterval]](_.asDouble, DoubleValue.apply)
    given CompareOps[Value, Value]  = new LiftedCompareOps[Value, Value, Topped[DoubleInterval], Topped[Boolean]](_.asDouble, BooleanValue.apply)
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

    new IntervalAnalysis(using effects)(using fixpoint) {}
  }

import IntervalAnalysis.*
class IntervalAnalysis
  (using effectOps: Effects)
  (using fix: Fix)
  (using boolOps: BooleanOps[Value], doubleOps: DoubleOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value])
  extends GenericInterpreter[Value, PowAddr, Effects, Fix]
