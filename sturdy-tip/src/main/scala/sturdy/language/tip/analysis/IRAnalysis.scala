package sturdy.language.tip.analysis

import sturdy.control.ControlObservable
import sturdy.data.{MayJoin, WithJoin, given}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.ObservableExcept
import sturdy.effect.failure.{CollectedFailures, Failure, ObservableFailure}
import sturdy.effect.print.{Print, PrintBound, given}
import sturdy.effect.{store, given}
import sturdy.effect.store.{*, given}
import sturdy.effect.userinput.AUserInput
import sturdy.fix.StackConfig
import sturdy.ir.{*, given}
import sturdy.language.tip.abstractions.*
import sturdy.language.tip.{AllocationSite, Field, FixIn, FixOut, TipFailure, *, given}
import sturdy.util.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import sturdy.{Executor, data, fix}

object IRAnalysis extends Interpreter,
  Ints.IRVal, Functions.Powerset, Records.IRRecords, References.IRRef, Fix:

  override type J[A] = WithJoin[A]

  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])
  
  def valueToIR(v: Value): IR = v match
    case Value.TopValue => IR.Unknonwn()
    case Value.BoolValue(b) => b
    case Value.IntValue(i) => i
    case Value.RefValue(addr) => addr
    case Value.FunValue(fun) => fun.set.map(IR.Const.apply).reduce(IR.Join.apply)
    case Value.RecValue(rec) => rec

  class Instance(initEnvironment: Environment, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    override def jv: WithJoin[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
    override val intOps: IntegerOps[Int, Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = new ReferenceOps[Addr, Value]:
      override def mkNullRef: IRAnalysis.Value = ???
      override def mkRef(trg: PowersetAddr[AllocationSiteAddr, AllocationSiteAddr]): IRAnalysis.Value = ???
      override def mkManagedRef(trg: PowersetAddr[AllocationSiteAddr, AllocationSiteAddr]): IRAnalysis.Value = ???
      override def deref(v: IRAnalysis.Value): PowersetAddr[AllocationSiteAddr, AllocationSiteAddr] = ???
    override val recOps: RecordOps[Field, Value, Value] = new RecordOps[Field, Value, Value]:
      override def makeRecord(fields: Seq[(Field, IRAnalysis.Value)]): IRAnalysis.Value = ???
      override def lookupRecordField(rec: IRAnalysis.Value, field: Field): IRAnalysis.Value = ???
      override def updateRecordField(rec: IRAnalysis.Value, field: Field, newval: IRAnalysis.Value): IRAnalysis.Value = ???
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: JoinableDecidableCallFrame[String, String, Value, Exp.Call] = new JoinableDecidableCallFrame("$main", Iterable.empty)
    override val store: AStoreThreaded[AllocationSiteAddr, Addr, Value] = new AStoreThreaded(initStore)
    override val alloc: AAllocatorFromContext[AllocationSite, Addr] = new AAllocatorFromContext(site =>
      PowersetAddr(References.allocationSiteAddr(site))
    )
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInput[Value] = new AUserInput(Value.IntValue(IR.Unknonwn()))

    var bounds: Set[Int] = Set()
    given Widen[VInt] = new Combine[VInt, Widening.Yes]:
      override def apply(v1: IR, v2: IR): MaybeChanged[IR] = ???
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def execute(p: Program): Value =
      bounds = p.intLiterals
      super.execute(p)

    override def copyState(from: Executor): Unit = {
      super.copyState(from)
      bounds = from.asInstanceOf[Instance].bounds
    }

    final override val fixpoint =
      callSiteSensitive(callSites,
        fix.dispatch(isFunOrWhile, Seq(
          fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)
        ))
      ).fixpoint

    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig, callSites)
