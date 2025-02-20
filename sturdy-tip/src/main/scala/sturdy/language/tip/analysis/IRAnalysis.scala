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
  Ints.IRVal, Functions.IRFun, Records.IRRecords, References.IRRef, Fix:

  override type J[A] = WithJoin[A]

  def valueToIR(v: Value): IR = v match
    case Value.TopValue => IR.Unknown()
    case Value.BoolValue(b) => b
    case Value.IntValue(i) => i
    case Value.RefValue(addr) => addr
    case Value.FunValue(fun) => fun
    case Value.RecValue(rec) => rec

  class Instance(initEnvironment: Environment, initStore: InitStore, stackConfig: StackConfig, callSites: Int) extends GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    private var currentCond: Option[IR] = None
    private var wideningStack: List[List[Value]] = List.empty
    private var count = 0


    given IRBooleanBranching[R](using Join[R]): BooleanBranching[IR, R] with
      override def boolBranch(cond: IR, thn: => R, els: => R): R =
        val condBefore = currentCond

        val r = try {
          currentCond = Some(cond)
          joinComputations { thn } { els }
        } finally {
          currentCond = condBefore
        }
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
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] =  new FunctionOps[Function, Seq[Value], Value, Value] {
      override def funValue(fun: Function): IRAnalysis.Value = IRAnalysis.Value.FunValue(IR.Const(fun))
      override def invokeFun(fun: IRAnalysis.Value, a: Seq[IRAnalysis.Value])(invoke: (Function, Seq[IRAnalysis.Value]) => IRAnalysis.Value): IRAnalysis.Value = fun match
        case Value.FunValue(fun) => fun match
          case IR.Const(f: Function) => invoke(f, a)
          case _ => Value.IntValue(IR.Op(IRFunctionOperator.CALL(invoke), fun +: a.map(valueToIR)))
        case _ => ???
    }
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

    override val callFrame: JoinableDecidableCallFrame[String, String, Value, Exp.Call] = new JoinableDecidableCallFrame[String, String, Value, Exp.Call]("$main", Iterable.empty) {
      override def widen: Widen[List[Value]] = (v1: List[Value], v2: List[Value]) =>

        println(s"v1 : $v1")
        println(s"v2 : $v2")
        println(s"stack : $wideningStack")
        println(s"cond : $currentCond")
        println(s"count : $count")
        count += 1

        val currentFeedback: Option[IR.Feedback] = v1.map(valueToIR).collectFirst{case v: IR.FeedbackAsk => v.feedback}.flatten // FIXME
        val currentFeedbackAsks: List[Option[IR.FeedbackAsk]] = v1.map {
          case Value.TopValue => None
          case v => valueToIR(v) match
            case a: IR.FeedbackAsk if a.feedback == currentFeedback => Some(a)
            case _ => None
        }
        val refineFeedback = currentFeedbackAsks.exists(_.isDefined)

        if (refineFeedback) {
          println("branch 1")

          val done = v1.zip(v2).filter {
            case (Value.TopValue, _) => false
            case _ => true
          }.map((v1, v2) => (valueToIR(v1), valueToIR(v2))).forall((n1, n2) => n1.feedbackReroll(n2))

          if (done || count > 5){ //  if v2 is unrolling of v1
            // wideningStack = wideningStack.tail
            Unchanged (v1)
          } else {
            val newVals = v2.map(valueToIR)
            println(s"  old step ${currentFeedback.get.step}")
            println(s"  new step $newVals")

            if (currentFeedback.get.step == newVals) {
              Unchanged(v1)
            } else {
              // Changed v1 with cond and steps updated
              currentFeedback.get.cond = currentCond
              currentFeedback.get.step = newVals
            }
            Changed(v1)
          }


        }

        else {

          println("branch 2")

          val feedback: IR.Feedback = IR.Feedback(v1.map(valueToIR), None, List.empty)
          val asks = v1.zipWithIndex.map((v, i) => v match
            case Value.TopValue => Value.TopValue
            case Value.BoolValue(_) => Value.BoolValue(IR.FeedbackAsk(i, Some(feedback)))
            case Value.IntValue(_) => Value.IntValue(IR.FeedbackAsk(i, Some(feedback)))
            case Value.RefValue(_) => Value.RefValue(IR.FeedbackAsk(i, Some(feedback)))
            case Value.FunValue(_) => Value.FunValue(IR.FeedbackAsk(i, Some(feedback)))
            case Value.RecValue(_) => Value.RecValue(IR.FeedbackAsk(i, Some(feedback)))
          )

          wideningStack = v1 :: wideningStack
          Changed(asks)
        }

    }

    override val store: AStoreThreaded[AllocationSiteAddr, Addr, Value] = new AStoreThreaded[AllocationSiteAddr, Addr, Value](initStore)
    override val alloc: AAllocatorFromContext[AllocationSite, Addr] = new AAllocatorFromContext(site =>
      PowersetAddr(References.allocationSiteAddr(site))
    )
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInputFun[Value] = new AUserInputFun(Value.IntValue(IR.Unknown())) with WithNamedUserInput(name => Value.IntValue(IR.External(name)))

    var bounds: Set[Int] = Set()
    given Widen[IR] = new Combine[VInt, Widening.Yes]:
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
