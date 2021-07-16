package sturdy.language.whilelang.analysis

import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.branching.ABoolBranching
import sturdy.effect.environment.AEnvironmentDynamicScope
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.failure.{Failure, AFailureCollect}
import sturdy.fix.CFixpoint
import sturdy.language.whilelang.{GenericInterpreter, Statement}
import sturdy.util.{Label, given}
import sturdy.values.JoinValue
import sturdy.values.booleans.{_, given}
import sturdy.values.doubles.{_, given}
import sturdy.values.relational.{_, given}
import sturdy.values.{Topped, given}
import sturdy.values.Topped.{_, given}

object SignAnalysis:
  enum Value:
    case BooleanValue(b: Topped[Boolean])
    case DoubleValue(d: Sign)

    def asBoolean: Topped[Boolean] = this match {
      case BooleanValue(b) => b
      case _ => throw new IllegalArgumentException(s"Expected Boolean but got $this")
    }
    def asDouble: Sign = this match {
      case DoubleValue(d) => d
      case _ => throw new IllegalArgumentException(s"Expected Double but got $this")
    }
  import Value._

  given JoinValue[Value] with
    override def joinValues(v1: Value, v2: Value): Value = (v1, v2) match
      case (BooleanValue(b1), BooleanValue(b2)) => BooleanValue(flatToppedJoin.joinValues(b1, b2))
      case (DoubleValue(d1), DoubleValue(d2)) => DoubleValue(SignJoin.joinValues(d1, d2))
      case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")



  type Context = Label
  type Addr = Set[Label]
  type Environment =  Map[String, (Boolean, Addr)]
  type Store = Map[Label, (Boolean, Value)]
  class Effects(initEnvironment: Environment, initStore: Store)
    extends ABoolBranching[Value]
    with AEnvironmentDynamicScope[String, Addr](initEnvironment)
    with AStoreMultiAddrThreadded[Label, Addr, Value](initStore)
    with AAllocationFromContext[Addr, Context](Set(_))
    with AFailureCollect
  type Fix = CFixpoint[Statement, Unit]

  def apply(initEnvironment: Environment, initStore: Store) = {
    val effects = new Effects(initEnvironment, initStore)
    val fixpoint = new CFixpoint[Statement, Unit]

    given Failure = effects
    given BooleanOps[Value] = new LiftedBooleanOps[Value, Topped[Boolean]](_.asBoolean, BooleanValue.apply)
    given DoubleOps[Value] = new LiftedDoubleOps[Value, Sign](_.asDouble, DoubleValue.apply)
    given CompareOps[Value, Value]  = new LiftedCompareOps[Value, Value, Sign, Topped[Boolean]](_.asDouble, BooleanValue.apply)
    given EqOps[Value, Value] with
      def equ(v1: Value, v2: Value): Value = BooleanValue((v1, v2) match
        case (BooleanValue(b1), BooleanValue(b2)) => summon[EqOps[Topped[Boolean], Topped[Boolean]]].equ(b1, b2)
        case (DoubleValue(d1), DoubleValue(d2)) => summon[EqOps[Sign, Topped[Boolean]]].equ(d1, d2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
      )
      def neq(v1: Value, v2: Value): Value = BooleanValue((v1, v2) match
        case (BooleanValue(b1), BooleanValue(b2)) => summon[EqOps[Topped[Boolean], Topped[Boolean]]].neq(b1, b2)
        case (DoubleValue(d1), DoubleValue(d2)) => summon[EqOps[Sign, Topped[Boolean]]].neq(d1, d2)
        case _ => throw new IllegalArgumentException(s"Expected values of equal type but got $v1 and $v2")
    )

    new GenericInterpreter(using effects)(using fixpoint) {}
  }

import SignAnalysis.*
class SignAnalysis
  (using effectOps: Effects)
  (using fix: Fix)
  (using boolOps: BooleanOps[Value], doubleOps: DoubleOps[Value], compareOps: CompareOps[Value, Value], eqOps: EqOps[Value, Value])
  extends GenericInterpreter[Value, Addr, Effects, Fix]
