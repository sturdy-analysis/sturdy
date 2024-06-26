package sturdy.language.tip.analysis

import sturdy.control.{ControlEvent, ControlObservable, RecordingControlObserver}
import sturdy.data.{WithJoin, given}
import sturdy.effect.given
import sturdy.effect.allocation.AAllocatorFromContext
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind, ObservableFailure}
import sturdy.effect.print.PrintFiniteAlphabet
import sturdy.effect.print.given
import sturdy.effect.store.AStoreThreaded
import sturdy.effect.store.Store
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.context.FiniteParameters
import sturdy.fix.{StackConfig, given}
import sturdy.language.tip.TipFailure
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.util.{*, given}
import sturdy.language.tip.{*, given}
import sturdy.language.tip.{AllocationSite, Field, FixIn, FixOut}
import sturdy.language.tip.abstractions.*
import sturdy.language.tip.analysis.IntervalAnalysis.controlEventLogger

import sturdy.values.integer.SignIntegerOps

object SignAnalysis extends Interpreter,
  Ints.Sign, Functions.Powerset, Records.PreciseFieldsOrTop, References.AllocationSites, Fix, Control:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue)

  class SignElaborationOps extends ElaborationOps[IntSign]{
    override def getCheck(x: Exp.Var, uv: IntSign): Exp = uv match {
      case IntSign.Pos => Exp.Gt(x, Exp.NumLit(0))
      case IntSign.Neg => Exp.Gt(Exp.NumLit(0), x)
      case IntSign.Zero => Exp.Eq(x, Exp.NumLit(0))
      case IntSign.TopSign => Exp.NumLit(1)
    }
  }

  class Instance(initEnvironment: Environment, initStore: InitStore, stackConfig: StackConfig) extends GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)

    val gl = gradualLogger[IntSign]()
    given GradualOps[IntSign, Value] with { val logger = gl}
    val eo = new SignElaborationOps();


    override val intOps: IntegerOps[Int, Value] = new LiftedIntegerOps[Int, Value, VInt](_.asInt, Value.IntValue.apply)(using new UnsafeSignIntegerOps[Int, Value]())
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly


    override val callFrame: JoinableDecidableCallFrame[String, String, Value, Exp.Call] = new JoinableDecidableCallFrame("$main", Iterable.empty)
    override val store: AStoreThreaded[AllocationSiteAddr, Addr, Value] = new AStoreThreaded(initStore)
    override val alloc: AAllocatorFromContext[AllocationSite, Addr] = new AAllocatorFromContext(site =>
      PowersetAddr(References.allocationSiteAddr(site))
    )
    override val print: PrintFiniteAlphabet[Value] = new PrintFiniteAlphabet
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(IntSign.TopSign))

    given Lazy[Finite[Value]] = lazily(FiniteValue)

    override val fixpoint =
      fix.log(gl,
        fix.log(controlEventLogger(this),
          fix.filter((dom: FixIn) => isFunOrWhile(dom) >= 0,
            parameterSensitive(this, fix.iter.innermost(stackConfig))))
      ).fixpoint
    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig)

  class DAIInstance(initEnvironment: Environment, initStore: InitStore) extends Instance(initEnvironment, initStore, StackConfig.StackedStates()):
    override val fixpoint = new fix.DAIFixpoint((dom: FixIn) => isFunOrWhile(dom))
    override def newInstance: sturdy.Executor = new DAIInstance(initEnvironment, initStore)


