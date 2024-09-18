package sturdy.language.pcf

import sturdy.data.{NoJoin, given}
import sturdy.effect.EffectStack
import sturdy.effect.environment.ConcreteEnvironment
import sturdy.effect.failure.{CollectedFailures, ConcreteFailure, Failure}
import sturdy.effect.store.StandardStore
import sturdy.effect.userinput.CUserInput
import sturdy.fix.StackConfig.StackedStates
import sturdy.fix
import sturdy.util.Label
import sturdy.values.{Abstractly, Finite, Join, Powerset, Topped, given}
import sturdy.values.booleans.{BooleanBranching, given}
import sturdy.values.closures.{Closure, ClosureOps, given}
import sturdy.values.integer.{IntegerOps, given}
import sturdy.values.ordering.{EqOps, OrderingOps, given}

object ConstantInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]

  type Var = String
  override type Addr = Label
  override type Env = Map[Var, Addr]

  override type VInt = Topped[Int]
  override type VBoolean = Topped[Boolean]
  override type VClosure = Powerset[Closure[Var, Exp, Env]]

  given Finite[Var] with {}
  given Finite[Addr] with {}
  given Finite[VClosure] with {}

  override def asBoolean(v: Value)(using Failure): VBoolean = v match
    case Value.Int(Topped.Actual(i)) => Topped.Actual(i != 0)
    case Value.Int(Topped.Top) => Topped.Top
    case _ => Failure(PCFFailure.TypeError, s"Expected Boolean but got $v")

  override def boolean(b: VBoolean): Value = b match
    case Topped.Actual(v) => Value.Int(Topped.Actual(if (v) 1 else 0))
    case Topped.Top => Value.Int(Topped.Top)

  given Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.Int(i) => Value.Int(Topped.Actual(i))
      case ConcreteInterpreter.Value.Closure(cl) =>
        val acl = Closure(cl.params, cl.body, cl.env)
        Value.Closure(Powerset(acl))

  class Instance(nextInput: () => Value) extends GenericInstance:
    override def jv: NoJoin[Value] = implicitly

    override def newAddr(l: Exp): Addr = l.label

    override val failure: CollectedFailures[PCFFailure] = new CollectedFailures[PCFFailure]
    given Failure = failure

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val orderingOps: OrderingOps[Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Value] = implicitly
    override val closureOps: ClosureOps[String, Exp, Env, Value, Value] = implicitly

    override val input: CUserInput[Value] = new CUserInput(nextInput)
    override val environment = new ConcreteEnvironment[Var, Addr](Map.empty)
    override val store = new StandardStore[Addr, Value](Map.empty)

    override val fixpoint =
      fix.filter[FixIn, Value](_.isInstanceOf[FixIn.Enter],
        fix.notContextSensitive(
          fix.iter.innermost[FixIn, Value, Unit](StackedStates())
        )).fixpoint

