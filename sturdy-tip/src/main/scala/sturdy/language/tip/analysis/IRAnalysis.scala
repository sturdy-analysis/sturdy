package sturdy.language.tip.analysis

import sturdy.control.ControlObservable
import sturdy.data.{MayJoin, WithJoin, joinComputations, given}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.ObservableExcept
import sturdy.effect.failure.{CollectedFailures, Failure, ObservableFailure}
import sturdy.effect.print.{Print, PrintBound, given}
import sturdy.effect.{EffectStack, store, given}
import sturdy.effect.store.{*, given}
import sturdy.effect.userinput.{AUserInput, AUserInputFun, WithNamedUserInput}
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

  def valueToIR(v: Value): IR = v match
    case Value.TopValue => IR.Unknonwn()
    case Value.BoolValue(b) => b
    case Value.IntValue(i) => i
    case Value.RefValue(addr) => addr
    case Value.FunValue(fun) => fun.set.map(IR.Const.apply).reduce(IR.Join.apply)
    case Value.RecValue(rec) => rec

  class Instance(initEnvironment: Environment, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    private var currentCond: Option[IR] = None
    private var currentCondWiden: Option[(IR, Boolean)] = None

    given IRBooleanBranching[R](using Join[R]): BooleanBranching[IR, R] with
      override def boolBranch(cond: IR, thn: => R, els: => R): R =
        val vars = callFrame.getState
        var fvars: Option[List[Value]] = None
        var gvars: Option[List[Value]] = None

        val condBefore = currentCond
        val conWidenBefore = currentCondWiden
        val r = try {
          joinComputations {
            currentCondWiden = Some(cond -> true)
            val rThn = thn
            fvars = Some(callFrame.getState)
            rThn
          } {
            currentCondWiden = Some(cond -> false)
            val rEls = els
            currentCond = Some(cond)
            gvars = Some(callFrame.getState)
            rEls
          }
        } finally {
          currentCond = condBefore
          currentCondWiden = conWidenBefore
        }
        println(s"boolBranch($cond)")
        println(s"  vars before = $vars")
        println(s"       cond   = $cond")
        println(s"  vars then   = $fvars")
        println(s"  vars else   = $gvars")
        println(s"  vars after  = ${callFrame.getState}")
        println(s"     result   = $r")

        r
    given Join[IR] = (v1: IR, v2: IR) => currentCond match
      case None => Changed(IR.Join(v1, v2))
      case Some(cond) => Changed(IR.Select(cond, v2, v1))

    override def jv: WithJoin[Value] = implicitly
    given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])


    override val failure: CollectedFailures[TipFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
    override val intOps: IntegerOps[Int, Value] = implicitly
    override val boolOps: BooleanOps[Value] = implicitly
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
    override val input: AUserInputFun[Value] = new AUserInputFun(Value.IntValue(IR.Unknonwn())) with WithNamedUserInput(name => Value.IntValue(IR.External(name)))

    var bounds: Set[Int] = Set()
    given Widen[IR] = new Combine[VInt, Widening.Yes]:
      var count = 0
      override def apply(v1: IR, v2: IR): MaybeChanged[IR] = (v1, v2) match
        case (feedback@IR.Feedback(init, cond, Some(other2)), other) =>
          println(s"Widening, stable feedback loop ?")
          println(s"  v1 = $v1")
          println(s"  v2 = $other")
          println(s"  other2 = $other2")
          println(s"  cond = $currentCond")
          println(s"  condWiden = $currentCondWiden")

//          println(sturdy.ir.Export.toGraphViz(feedback))
//          println(sturdy.ir.Export.toGraphViz(other))

          if (other == other2 || true) {
            Unchanged(feedback)
          } else
            ???
        case (feedback@IR.Feedback(init, None, None), other) =>
          println(s"Widening, make feedback loop")
          println(s"  v1 = $v1")
          println(s"  v2 = $v2")
          println(s"  cond = $currentCond")
          println(s"  condWiden = $currentCondWiden")


          feedback.loop = Some(other)
          feedback.cond = currentCondWiden match
            case Some((c, true)) => Some(c)
            case Some((c, false)) => Some(IR.Op(???, c))
            case None => throw new IllegalStateException()

//          println(sturdy.ir.Export.toGraphViz(feedback))
          Changed(feedback)
        case _ =>
          println(s"Widening, prepare feedback loop")
          println(s"  v1 = $v1")
          println(s"  v2 = $v2")
          println(s"  cond = $currentCond")
          println(s"  condWiden = $currentCondWiden")

          val select = currentCondWiden match
            case Some((c, true)) => IR.Select(c, v2, v1)
            case Some((c, false)) => IR.Select(c, v1, v2)
            case None =>
              throw new IllegalStateException() // IR.Select(IR.Unknonwn(), v1, v2)

          val feedback = IR.Feedback(select, None, None)
//          println(sturdy.ir.Export.toGraphViz(feedback))
          if (count > 3)
            ???
          count += 1
          Changed(feedback)

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
