package sturdy.language.tip.analysis

import sturdy.data.{WithJoin, given}
import sturdy.effect.AnalysisState
import sturdy.effect.{Effectful, AnalysisState, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.JoinableConcreteCallFrame
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.print.{APrintPrefix, given}
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.Fixpoint
import sturdy.fix.given
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.GenericInterpreter.{Field, FixIn, AllocationSite, FixOut, given}
import sturdy.language.tip.abstractions.*

object SignAnalysis extends Interpreter,
  Ints.Sign, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue)

  abstract class Instance(initEnvironment: Environment, initStore: Store) extends GenericInstance:
    override def jv: WithJoin[Value] = implicitly

    final def vintOps: IntegerOps[Int, VInt] = implicitly
    final def vcompareOps: OrderingOps[VInt, VBool] = implicitly
    final def vintEqOps: EqOps[VInt, VBool] = implicitly
    final def vrefEqOps: EqOps[VRef, VBool] = implicitly
    final def vfunEqOps: EqOps[VFun, VBool] = implicitly
    final def vrecEqOps: EqOps[VRecord, VBool] = ??? // new ARecordEqOps(using lazily(eqOps))
    final def vfunOps: FunctionOps[Function, Seq[Value], Value, VFun] = implicitly
    final def vrefOps: ReferenceOps[Addr, VRef] = implicitly
    final def vrecOps: RecordOps[Field, Value, VRecord] = implicitly
    final def vbranchOps: BooleanBranching[Topped[Boolean], Unit] = implicitly

    override val callFrame: JoinableConcreteCallFrame[Unit, String, Addr] = new JoinableConcreteCallFrame((), initEnvironment)
    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(initStore)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: APrintPrefix[Value] = new APrintPrefix
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(IntSign.TopSign))

//    given Join[InState] = implicitly
//    given Join[OutState] = implicitly
//    given Widen[InState] = implicitly
//    given Widen[OutState] = implicitly
    given Lazy[Widen[Value]] = lazily(CombineValue)

    
