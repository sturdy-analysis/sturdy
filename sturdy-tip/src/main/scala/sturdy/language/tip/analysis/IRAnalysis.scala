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

  def foreachIrValue(v: Value)(f: IR => Unit): Unit = v match
    case Value.TopValue => f(IR.Unknown())
    case Value.IntValue(v) => f(v)
    case Value.RefValue(v) => // nothing
    case Value.FunValue(v) => // nothing
    case Value.RecValue(v) => // nothing

  def selectIR(v: Value): Option[IR] = v match
    case Value.IntValue(i) => Some(i)
    case _ => None

  def structuralEquals(v1: Value, v2: Value): Boolean =
    import Value.*
    (v1, v2) match
      case (IntValue(i1), IntValue(i2)) =>
        val bool = i1.structuralEquality(i2)
        println(s"$bool for $i1 and $i2")
        bool
      case _ => v1 == v2

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
          new CustomCombinator(fix.iter.innermost[FixIn, FixOut[Value], Unit](stackConfig)),
          new CustomCombinator(fix.iter.innermost[FixIn, FixOut[Value], Unit](stackConfig))
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
        with PathSensitiveCallFrame[String, String, Value, tip.Exp.Call] {

        override def widen: Widen[List[Value]] = new Widen[List[Value]] {
          override def apply(v1: List[Value], v2: List[Value]): MaybeChanged[List[Value]] =
            // if v2 is just an unrolling of the body of the feedback node of v1
            if (v1.length == v2.length && v1.zip(v2).forall(structuralEquals.tupled)) // TODO : Cleaner implementation
              Unchanged(v1)
            else {
              val feedback = currentFeedback.get._2
              if (irBranchOps.inElse)
                feedback.cond = irBranchOps.currentCond.map(cond => IR.Op(IRBooleanOperator.NOT, Array(cond)))
              else
                feedback.cond = irBranchOps.currentCond
              feedback.steps = Some(v2.map(selectIR.andThen(_.getOrElse(IR.Unknown()))))
              val res = v1.zip(v2).zipWithIndex.map((v, i) => (v._1, v._2) match
                case (Value.IntValue(IR.Undefined()), Value.IntValue(ir2)) if ir2 != IR.Undefined() =>
                  Value.IntValue(IR.FeedbackAsk(i, feedback))
                case _ => v._1
              )

              val isEqual = v1.zip(res).forall(structuralEquals.tupled)
              println(s"Widen callframe (changed = ${!isEqual}):")
              res.foreach(v => foreachIrValue(v) { ir => println(Export.toGraphViz(ir)) })
              println(s"v1 was")
              v1.foreach(v => foreachIrValue(v) { ir => println(Export.toGraphViz(ir)) })
              println(s"v2 was")
              v2.foreach(v => foreachIrValue(v) { ir => println(Export.toGraphViz(ir)) })
              MaybeChanged(res, !isEqual)
            }
        }
      }

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

    var currentFeedback: Option[(FixIn, IR.Feedback)] = None
    class CustomCombinator(phi : Combinator[FixIn, FixOut[Value]]) extends Combinator[FixIn, FixOut[Value]] :
      override def apply(v1: FixIn => FixOut[Value]): FixIn => FixOut[Value] = fixIn => {
        if (currentFeedback.exists(_._1 == fixIn))
          phi(v1)(fixIn)
        else
          val feedback : IR.Feedback = IR.Feedback(
            callFrame.getState.map(selectIR.andThen(_.getOrElse(IR.Unknown()))),
            None,
            None)

          val beforeFeedback = currentFeedback
          try {
            currentFeedback = Some(fixIn, feedback)
            callFrame.setState(callFrame.getState.zipWithIndex.map {
              case (Value.IntValue(IR.Undefined()), _) => Value.IntValue(IR.Undefined())
              case (Value.IntValue(_), i) => Value.IntValue(IR.FeedbackAsk(i, feedback))
              case (v, _) => v
            })
            phi(v1)(fixIn)
          }
          finally {
            currentFeedback = beforeFeedback
          }
      }