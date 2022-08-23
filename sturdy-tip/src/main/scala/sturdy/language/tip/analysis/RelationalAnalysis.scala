package sturdy.language.tip.analysis

import apron.Manager
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Texpr1Node
import sturdy.Executor
import sturdy.data.{WithJoin, given}
import sturdy.effect.{EffectStack, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.ApronCallFrame
import sturdy.effect.callframe.ApronCallFrame.given
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.CollectedFailures
import sturdy.effect.failure.Failure
import sturdy.effect.print.PrintBound
import sturdy.effect.print.given
import sturdy.effect.store.AStoreMultiAddrThreadded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.apron.{Apron, given}
import sturdy.fix
import sturdy.fix.StackConfig
import sturdy.fix.context
import sturdy.language.tip.AllocationSite
import sturdy.language.tip.*
import sturdy.language.tip.abstractions.{Fix, Functions, Records, References, isFunOrWhile}
import sturdy.util.Lazy
import sturdy.util.lazily
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}


object RelationalAnalysis extends Interpreter,
  Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix:

  override type J[A] = WithJoin[A]
  override type VInt = Texpr1Node
  override type VBool = Topped[Tcons1]

  final def asBoolean(v: Value)(using inst: Instance): VBool = v match
    case Value.BoolValue(b) => b
    case Value.IntValue(i) =>
     // TODO Replace DISEQ
      Topped.Actual(inst.apron.makeConstraint(i, Tcons1.DISEQ))
    case Value.TopValue =>
      Topped.Top
    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  final def asInt(v: Value)(using inst: Instance): VInt = v match
    case Value.BoolValue(Topped.Top) =>
      import inst.{given_EffectStack, apron, failure}
      given Failure = failure
      val vIntOps = summon[IntegerOps[Int, VInt]]
      Join(vIntOps.integerLit(1), vIntOps.integerLit(0)).get
    case Value.BoolValue(Topped.Actual(b)) =>
      import inst.{given_EffectStack, apron, failure}
      given Failure = failure
      val vIntOps = summon[IntegerOps[Int, VInt]]
      inst.apron.ifThenElse(b) {
        vIntOps.integerLit(1)
      } {
        vIntOps.integerLit(0)
      }
    case Value.IntValue(i) => i
    case Value.TopValue =>
      inst.apron.topInt
    case _ => inst.failure(TipFailure.TypeError, s"Expected Int but got $this")

  override def topInt(using inst: Instance): VInt = inst.apron.topInt
  override def topBool: VBool = Topped.Top

  class Instance(apronManager: Manager, initEnvironment: Environment, initStore: Store, stackConfig: StackConfig, callSites: Int) extends GenericInstance:
    given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

    implicit val apron: Apron = new Apron(apronManager)
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)

    given EqOps[VRef, VBool] with
      override def equ(v1: Powerset[AllocationSiteRef], v2: Powerset[AllocationSiteRef]): Topped[Tcons1] = topBool
      override def neq(v1: Powerset[AllocationSiteRef], v2: Powerset[AllocationSiteRef]): Topped[Tcons1] = topBool

    given EqOps[VFun, VBool] with
      override def equ(v1: Powerset[Function], v2: Powerset[Function]): Topped[Tcons1] = topBool
      override def neq(v1: Powerset[Function], v2: Powerset[Function]): Topped[Tcons1] = topBool

    given EqOps[VRecord, VBool] with
      override def equ(v1: ARecord[Field, RelationalAnalysis.Value], v2: ARecord[Field, RelationalAnalysis.Value]): Topped[Tcons1] = topBool
      override def neq(v1: ARecord[Field, RelationalAnalysis.Value], v2: ARecord[Field, RelationalAnalysis.Value]): Topped[Tcons1] = topBool

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: ApronCallFrame[Unit, String, Value] = new ApronCallFrame(
      apron,
      (),
      { case Value.IntValue(t) => Some(t); case _ => None },
      _ => None,
      Value.IntValue.apply,
      _ => Value.TopValue,
      initEnvironment
    )

    override val store: AStoreMultiAddrThreadded[AllocationSiteAddr, Value] = new AStoreMultiAddrThreadded(initStore)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(apron.topInt))

    // TODO check
    given Widen[VInt] = new WideningTexpr1Node
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def execute(p: Program): Value =
      super.execute(p)

    override def copyState(from: Executor): Unit = {
      super.copyState(from)
    }

    final override val fixpoint =
      callSiteSensitive(callSites, fix.dispatch(isFunOrWhile, Seq(
        fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)))
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(apronManager, initEnvironment, initStore, stackConfig, callSites)