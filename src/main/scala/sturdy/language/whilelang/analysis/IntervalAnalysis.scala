package sturdy.language.whilelang.analysis

import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.environment.AEnvironmentDynamicScope
import sturdy.effect.store.AStoreKeysThreadded
import sturdy.effect.failure.AFailureCollect
import sturdy.fix.CFixpoint
import sturdy.language.whilelang.GenericInterpreter
import sturdy.language.whilelang.Statement
import sturdy.util.{Label, given}
import sturdy.values.domain.{_, given}
import sturdy.values.JoinValue
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


  given BooleanOps[Value] = new LiftedBooleanOps[Value, Topped[Boolean]](_.asBoolean, BooleanValue.apply)
  given DoubleOps[Value] = new LiftedDoubleOps[Value, Topped[DoubleInterval]](_.asDouble, DoubleValue.apply)
  given CompareOps[Value, Value]  = new LiftedCompareOps[Value, Value, Topped[DoubleInterval], Topped[Boolean]](_.asDouble, BooleanValue.apply)
  given EqOps[Value, Value] with
    def equ(v1: Value, v2: Value): Value = BooleanValue((v1, v2) match
      case (BooleanValue(b1), BooleanValue(b2)) => summon[EqOps[Topped[Boolean], Topped[Boolean]]].equ(b1, b2)
      case (DoubleValue(d1), DoubleValue(d2)) => summon[EqOps[Topped[DoubleInterval], Topped[Boolean]]].equ(d1, d2)
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    )
    def neq(v1: Value, v2: Value): Value = BooleanValue((v1, v2) match
      case (BooleanValue(b1), BooleanValue(b2)) => summon[EqOps[Topped[Boolean], Topped[Boolean]]].neq(b1, b2)
      case (DoubleValue(d1), DoubleValue(d2)) => summon[EqOps[Topped[DoubleInterval], Topped[Boolean]]].neq(d1, d2)
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    )

  type Context = Label
  type Addr = Label
  type Addrs = Set[Label]
  type Environment =  Map[String, (Boolean, Addrs)]
  type Store = Map[Addr, (Boolean, Value)]

  def effects(initEnvironment: Environment, initStore: Store) =
    new  ABoolBranching[Value]
    with AEnvironmentDynamicScope[String, Addrs](initEnvironment)
    with AStoreKeysThreadded[Addr, Addrs, Value](initStore)
    with AAllocationFromContext[Addrs, Context](Set(_))
    with AFailureCollect
  def fixpoint = new CFixpoint[Statement, Unit]

  def apply(initEnvironment: Environment, initStore: Store) =
    new GenericInterpreter(using effects(initEnvironment, initStore))(using fixpoint) {}
