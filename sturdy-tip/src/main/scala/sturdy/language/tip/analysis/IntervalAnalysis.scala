package sturdy.language.tip.analysis

import sturdy.data
import sturdy.data.MayJoin
import sturdy.data.{WithJoin, given}
import sturdy.effect.{Effectful, AnalysisState, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.allocation.Allocation
import sturdy.effect.callframe.DecidableCallFrame
import sturdy.effect.callframe.JoinedDecidableCallFrame
import sturdy.effect.failure.{AFailureCollect, Failure}
import sturdy.effect.print.Print
import sturdy.effect.print.{APrintPrefix, given}
import sturdy.effect.store
import sturdy.effect.store
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
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
import sturdy.language.tip.GenericInterpreter.{Field, FixIn, AllocationSite, FixOut}
import sturdy.language.tip.abstractions.*

object IntervalAnalysis extends Interpreter,
  Ints.Interval, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type J[A] = WithJoin[A]
  override type Ctx = CallString

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  type InState = Store
  type OutState = (Store, APrintPrefix.PrintResult[Value])
  type AllState = OutState

  class Instance(initEnvironment: Environment, initStore: Store, steps: Int) extends GenericInstance:
    override def jv: WithJoin[Value] = implicitly

    final def vintOps: IntegerOps[Int, VInt] = implicitly
    final def vcompareOps: OrderingOps[VInt, VBool] = implicitly
    final def vintEqOps: EqOps[VInt, VBool] = implicitly
    final def vrefEqOps: EqOps[VRef, VBool] = implicitly
    final def vfunEqOps: EqOps[VFun, VBool] = implicitly
    final def vrecEqOps: EqOps[VRecord, VBool] = ??? //new ARecordEqOps(using lazily(eqOps))
    final def vfunOps: FunctionOps[Function, Seq[Value], Value, VFun] = implicitly
    final def vrefOps: ReferenceOps[Addr, VRef] = implicitly
    final def vrecOps: RecordOps[Field, Value, VRecord] = implicitly
    final def vbranchOps: BooleanBranching[Topped[Boolean], Unit] = implicitly

    override val callFrame: JoinedDecidableCallFrame[Unit, String, Addr] = new JoinedDecidableCallFrame((), initEnvironment)
    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(initStore)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: APrintPrefix[Value] = new APrintPrefix
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(IntInterval.Top))

    var bounds: Set[Int] = Set.empty
    given Widen[IntInterval] = new IntIntervalWiden(bounds)
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def execute(p: Program): Value =
      bounds = p.intLiterals
      super.execute(p)

    val callSites = callSitesLogger()

    protected override def context = callSites.callString(2)
    protected override def contextFree = fix.log(callSites, _)
    override def contextSensitive = fix.dispatch(isFunOrWhile, Seq(
      // call
      fix.iter.topmost,
      // while
      fix.unwind(steps,
        fix.iter.innermost
      )
    ))
