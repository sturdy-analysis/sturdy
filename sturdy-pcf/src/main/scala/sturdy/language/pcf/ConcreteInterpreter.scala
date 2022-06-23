package sturdy.language.pcf

import sturdy.data.{NoJoin, given}
import sturdy.effect.environment.Box
import sturdy.effect.environment.ClosableEnvironment
import sturdy.effect.environment.ConcreteCyclicEnvironment
import sturdy.effect.failure.ConcreteFailure
import sturdy.effect.failure.Failure
import sturdy.effect.userinput.CUserInput
import sturdy.fix
import sturdy.values.booleans.{BooleanBranching, given}
import sturdy.values.closures.{Closure, ClosureOps, given}
import sturdy.values.integer.{IntegerOps, given}
import sturdy.values.relational.{EqOps, given}
import sturdy.values.relational.{OrderingOps, given}

object ConcreteInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]
  override type VInt = Int
  override type VBoolean = Boolean
  override type VClosure = Closure[String, Exp, Env]
  override type Env = Map[String, Box[Value]]

  override def asBoolean(v: Value)(using Failure): Boolean = v match
    case Value.Int(i) => i != 0
    case _ => Failure(TypeError, s"Expected Boolean but got $v")
  override def boolean(b: Boolean): Value = Value.Int(if b then 1 else 0)

  class Instance(nextInput: () => Value) extends GenericInstance:
    override def jv: NoJoin[Value] = implicitly

    override val failure: ConcreteFailure = new ConcreteFailure
    given Failure = failure

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val orderingOps: OrderingOps[Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Value] = implicitly
    override val closureOps: ClosureOps[String, Value, Exp, Env, Value, Value] = implicitly

    override val input: CUserInput[Value] = new CUserInput(nextInput)
    override val environment = new ConcreteCyclicEnvironment[String, Value](Map.empty)

    override val fixpoint = new fix.ConcreteFixpoint[Exp, Value]