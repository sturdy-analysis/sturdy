package sturdy.language.pcf

import sturdy.data.{NoJoin, given}
import sturdy.effect.EffectStack
import sturdy.effect.environment.{Box, ClosableEnvironment, ConcreteCyclicEnvironment, StandardCyclicEnvironment}
import sturdy.effect.failure.{CollectedFailures, ConcreteFailure, Failure}
import sturdy.effect.userinput.CUserInput
import sturdy.fix.StackConfig.StackedStates
import sturdy.{fix, values}
import sturdy.values.{Abstractly, Finite, Join, Powerset, Topped, given}
import sturdy.values.booleans.{BooleanBranching, given}
import sturdy.values.closures.{Closure, ClosureOps, given}
import sturdy.values.integer.{IntegerOps, given}
import sturdy.values.ordering.{EqOps, OrderingOps, given}

object ConstantInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]
  override type VInt = Topped[Int]
  override type VBoolean = Topped[Boolean]
  override type VClosure = Powerset[Closure[Var, Exp, Env]]
  // todo finite closure representation, maybe: Map[Lam, Env]
  override type Env = Map[Var, Box[Value]]

  type Var = String
  given Finite[Var] with {}

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
        val aenv: Env = cl.env.view.mapValues {
          case Box.Eager(v) => Box.Eager(this.apply(v))
          case Box.Lazy(v) => Box.Lazy(() => this.apply(v()))
        }.toMap
        val acl = Closure(cl.params, cl.body, aenv)
        Value.Closure(Powerset(acl))


  class Instance(nextInput: () => Value) extends GenericInstance:
    override def jv: NoJoin[Value] = implicitly

    override val failure: CollectedFailures[PCFFailure] = new CollectedFailures[PCFFailure]
    given Failure = failure

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val orderingOps: OrderingOps[Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Value] = implicitly
    override val closureOps: ClosureOps[String, Exp, Env, Value, Value] = implicitly

    override val input: CUserInput[Value] = new CUserInput(nextInput)
    override val environment = new StandardCyclicEnvironment[Var, Value](Map.empty)

    given Finite[VClosure] with {}

    override val fixpoint =
      fix.filter[FixIn, Value](_.isInstanceOf[FixIn.Enter],
        fix.notContextSensitive(
          fix.iter.innermost[FixIn, Value, Unit](StackedStates())
        )).fixpoint

