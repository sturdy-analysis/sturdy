package sturdy.language.pcf

import sturdy.data.{WithJoin, given}
import sturdy.effect.given
import sturdy.effect.environment.{Box, ClosableEnvironment, CyclicEnvironment, *}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.given
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.print.PrintFiniteAlphabet
import sturdy.effect.print.given
import sturdy.effect.store.may.PowersetAddrMayStore
import sturdy.effect.store.Store
import sturdy.effect.userinput.{AUserInput, CUserInput}
import sturdy.fix
import sturdy.fix.context.FiniteParameters
import sturdy.fix.{StackConfig, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.{*, given}
import sturdy.language.pcf.{*, given}
import sturdy.language.pcf.abstractions.{*, given}
import sturdy.values.closures.{Closure, ClosureOps, given}
import sturdy.values.booleans.{BooleanBranching, given}
import sturdy.values.closures.{Closure, ClosureOps, given}
import sturdy.values.integer.IntSign.TopSign
import sturdy.values.integer.{IntegerOps, given}
import sturdy.values.relational.{EqOps, given}
import sturdy.values.relational.{OrderingOps, given}
import sturdy.values.integer.{AbstractBitVector, IntSign, NumericInterval, given}
import sturdy.language.pcf.abstractions.Fix

object SignInterpreter extends Interpreter, Ints.Sign:
  override type J[A] = WithJoin[A]
  override type VClosure = Closure[String, Exp, Env]
  override type Env = Map[String, Box[Value]]
  final type Environment = Map[String, Value]

  given Lazy[Widen[Value]] = lazily(CombineValue)

  class Instance(initEnvironment: Environment, stackConfig: StackConfig) extends GenericInstance:
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[PCFFailure] = new CollectedFailures
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val orderingOps: OrderingOps[Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Value] = implicitly
    override val closureOps: ClosureOps[String, Exp, Env, Value, Value] = implicitly
    override val input: AUserInput[Value] = new AUserInput(Value.Int(TopSign))
    override val environment = implicitly
    override val fixpoint = new fix.ConcreteFixpoint[Exp, Value]
    given Lazy[Finite[Value]] = lazily(FiniteValue)


