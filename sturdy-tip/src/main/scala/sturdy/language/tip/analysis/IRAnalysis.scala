package sturdy.language.tip.analysis

import sturdy.data.MayJoin.WithJoin
import sturdy.data.{JOption, joinComputations, given}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.{DecidableCallFrame, JoinableDecidableCallFrame, PathSensitiveCallFrame}
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.print.{Print, PrintBound}
import sturdy.effect.store.{*, given}
import sturdy.effect.userinput.{AUserInput, AUserInputFun, UserInput, WithNamedUserInput}
import sturdy.fix.{Combinator, Fixpoint, StackConfig, notContextSensitive}
import sturdy.ir.{*, given}
import sturdy.language.tip
import sturdy.language.tip.abstractions.{Functions, Ints, Records, References, isFunOrWhile}
import sturdy.language.tip.{*, given}
import sturdy.util.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{*, given}
import sturdy.{Executor, data, fix}

object IRAnalysis extends Interpreter, Ints.IRInts, Functions.Powerset, References.AllocationSites, Records.PreciseFieldsOrTop:

  override type J[A] = WithJoin[A]
  given Lazy[Join[Value]] = lazily(CombineValue[Widening.No])

  def irValue(v: Value): IR = v match
    case Value.TopValue => IR.Unknown()
    case Value.IntValue(v) => v
    case Value.BoolValue(v) => v
    case Value.RefValue(v) => ???
    case Value.FunValue(v) => ???
    case Value.RecValue(v) => ???
  
  class Instance(stackConfig: StackConfig) extends GenericInstance:

    /*
      Version based on structural equality for fixpoint stability:
      - should work for simple and consecutive loops
      - and for use in products
      - works for simple recursive calls (one call site, tail recursion (value widening is not called)
      - but not for nested loops
    */

    implicit val irBranchOps: PathSensitiveBranching[IR, Unit] = new PathSensitiveBranching(c => IR.Op(IRBooleanOperator.NOT, c))

    given Widen[IR] = (v1: IR, v2: IR) => // Used only (?) for the return value of a recursive function
      if (v1.structuralEquality(v2))
        Unchanged(v1)
      else
        Changed(v2)
    given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

    override def jv: WithJoin[Value] = implicitly

    override val fixpoint =
      fix.notContextSensitive(
        fix.dispatch(isFunOrWhile, Seq(
          fix.iter.innermost[FixIn, FixOut[Value], Unit](stackConfig), fix.iter.innermost[FixIn, FixOut[Value], Unit](stackConfig)
        ))
      ).fixpoint

    override val intOps: IntegerOps[Int, Value] = implicitly
    override val boolOps: BooleanOps[Value] = implicitly
    override val compareOps: OrderingOps[Value, Value] = implicitly
    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    given PathSensitive[VFun] = NotPathSensitive()
    given PathSensitive[VRef] = NotPathSensitive()
    given PathSensitive[VRecord] = NotPathSensitive()
    
    override val callFrame: JoinableDecidableCallFrame[String, String, Value, Exp.Call] =
      new JoinableDecidableCallFrame[String, String, Value, tip.Exp.Call]("$main", Iterable.empty) 
        with PathSensitiveCallFrame[String, String, Value, tip.Exp.Call]

    override val store: AStoreThreaded[AllocationSiteAddr, Addr, Value] = new AStoreThreaded(Map.empty)
    override val alloc: AAllocatorFromContext[AllocationSite, Addr] = new AAllocatorFromContext(site =>
      PowersetAddr(References.allocationSiteAddr(site))
    )
    override val print: PrintBound[Value] = new PrintBound
    override val input: AUserInputFun[Value] = 
      new AUserInputFun(Value.IntValue(IR.Unknown())) with WithNamedUserInput(name => Value.IntValue(IR.External(name)))
    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    override def newInstance: Executor = new Instance(stackConfig)

//    class CustomCombinator(phi : Combinator[FixIn, FixOut[IR]]) extends Combinator[FixIn, FixOut[IR]] :
//      override def apply(v1: FixIn => FixOut[IR]): FixIn => FixOut[IR] = fixIn => {
//        if (currentFeedback.exists(_._1 == fixIn))
//          phi(v1)(fixIn)
//        else
//          val feedback : IR.Feedback = IR.Feedback(
//            callFrame.getState,
//            None,
//            None)
//
//          val beforeFeedback = currentFeedback
//          try {
//            currentFeedback = Some(fixIn, feedback)
//            callFrame.setState(callFrame.getState.zipWithIndex.map((v, i) => v match
//              case IR.Undefined() => IR.Undefined()
//              case _ => IR.FeedbackAsk(i, feedback)))
//            phi(v1)(fixIn)
//          }
//          finally {
//            currentFeedback = beforeFeedback
//          }
//      }