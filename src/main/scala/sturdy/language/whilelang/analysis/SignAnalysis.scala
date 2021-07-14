package sturdy.language.whilelang.analysis

import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.environment.AEnvironmentDynamicScope
import sturdy.effect.store.AStoreKeysThreadded
import sturdy.effect.failure.AFailureCollect
import sturdy.fix.CFixpoint
import sturdy.language.whilelang.GenericInterpreter
import sturdy.util.{Label, given}
import sturdy.values.domain.{_, given}
import sturdy.values.JoinValue
import sturdy.values.branch.BranchOps
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


  given BooleanOps[Value] = new LiftedBooleanOps[Value, Topped[Boolean]](_.asBoolean, BooleanValue.apply)
  given DoubleOps[Value] = new LiftedDoubleOps[Value, Sign](_.asDouble, DoubleValue.apply)
  given BranchOps[Value] with
    type BranchJoin[A] = JoinValue[A]
    def if_[A](v: Value, thn: => A, els: => A)(using j: JoinValue[A]): A = v.asBoolean match
      case Actual(true) => thn
      case Actual(false) => els
      case Top => j.joinValues(thn, els)
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

  type Context = Label
  type Addr = Label
  type Addrs = Set[Label]


import SignAnalysis._

//class SignAnalysis(initEnvironment: Map[String, Addrs], initStore: Map[Addr, Value]) extends GenericInterpreter[Value, Addrs]
//  (using new AEnvironmentDynamicScope[String, Addrs](Map())
//    with AStoreKeysThreadded[Addr, Addrs, Value](Map())
//    with AAllocationFromContext[Addrs, Context](Set(_))
//    with AFailureCollect)
//  (using new CFixpoint)
