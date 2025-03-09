package sturdy.language.tip.analysis

import sturdy.data.MayJoin.WithJoin
import sturdy.data.{JOption, joinComputations, given}
import sturdy.effect.allocation.{AAllocatorFromContext, Allocator}
import sturdy.effect.callframe.{DecidableCallFrame, JoinableDecidableCallFrame}
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.print.{Print, PrintBound}
import sturdy.effect.store.{*, given}
import sturdy.effect.userinput.{AUserInputFun, UserInput, WithNamedUserInput}
import sturdy.fix.{Combinator, Fixpoint, StackConfig, notContextSensitive}
import sturdy.ir.{*, given}
import sturdy.language.tip.abstractions.isFunOrWhile
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

object IRAnalysis:

  class Instance(stackConfig: StackConfig) extends GenericInterpreter[IR, IR, WithJoin]:

    private var currentCond : Option[IR] = None
    private var currentFeedback : Option[(FixIn, IR.Feedback)] = None // May be unnecessary
    /*
      Version based on structural equality for fixpoint stability:
      - should work for simple and consecutive loops
      - and for use in products
      - works for simple recursive calls (one call site, tail recursion (value widening is not called)
      - but not for nested loops
    */

    given Join[IR] = (v1: IR, v2: IR) => currentCond match
      case None => Changed(IR.Join(v1, v2))
      case Some(cond) => if v1.structuralEquality(v2) then Unchanged(v1) else Changed(IR.Select(cond, v2, v1))

    given Widen[IR] = (v1: IR, v2: IR) => // Used only (?) for the return value of a recursive function
      if v1.structuralEquality(v2) then Unchanged(v1) else Changed(v2)

    override def jv: WithJoin[IR] = implicitly

    override val fixpoint =
      notContextSensitive[FixIn, FixOut[IR], Combinator[FixIn, FixOut[IR]]](
        fix.dispatch(isFunOrWhile, Seq(
          new CustomCombinator(fix.iter.innermost(stackConfig)), new CustomCombinator(fix.iter.innermost(stackConfig))
        ))
      ).fixpoint

    override val intOps: IntegerOps[Int, IR] = implicitly
    override val boolOps: BooleanOps[IR] = implicitly
    override val compareOps: OrderingOps[IR, IR] = implicitly
    override val eqOps: EqOps[IR, IR] = implicitly
    override val functionOps: FunctionOps[Function, Seq[IR], IR, IR] = implicitly // new IRFunctionOps[Function, Seq[IR]](identity)

    override val refOps: ReferenceOps[IR, IR] = new ReferenceOps[IR, IR] { // TODO : Unimplemented
      override def mkNullRef: IR = ???
      override def mkRef(trg: IR): IR = ???
      override def mkManagedRef(trg: IR): IR = ???
      override def deref(v: IR): IR = ???
    }

    override val recOps: RecordOps[Field, IR, IR] = new RecordOps[Field, IR, IR] { // TODO : Records are not supported
      override def makeRecord(fields: Seq[(Field, IR)]): IR = ???
      override def lookupRecordField(rec: IR, field: Field): IR = ???
      override def updateRecordField(rec: IR, field: Field, newval: IR): IR = ???
    }

    override implicit val branchOps: BooleanBranching[IR, Unit] = new BooleanBranching[IR, Unit] {
      override def boolBranch(cond: IR, thn: => Unit, els: => Unit): Unit =
        val condBefore = currentCond
        try {
          currentCond = Some(cond)
          joinComputations {thn} {
            inElse = true
            els
          }
        }
        finally {
          inElse = false
          currentCond = condBefore
        }
    }

    override val callFrame: JoinableDecidableCallFrame[String, String, IR, Exp.Call] =
      new JoinableDecidableCallFrame[String, String, IR, Exp.Call]("$main", Iterable.empty) {

        override def setVars(newVars: Iterable[(String, Option[IR])]): Unit = { // TODO : Fix this ugly override that assigns Undefined as a default value to avoid inconvenient nulls.
          val builder = Map.newBuilder[String, Int]
          vars = Array.fill(newVars.size)(IR.Undefined())
          var i = 0
          for ((name, v) <- newVars) {
            builder += name -> i
            v.foreach(vars.update(i, _))
            i += 1
          }
          names = builder.result()
        }

        override def widen: Widen[List[IR]] = new Widen[List[IR]] {
          override def apply(v1: List[IR], v2: List[IR]): MaybeChanged[List[IR]] =
            // if v2 is just an unrolling of the body of the feedback node of v1
            if(v1.length == v2.length && v1.zip(v2).forall(v => v._1.structuralEquality(v._2))) // TODO : Cleaner implementation
              Unchanged(v1)
            else
              val feedback = currentFeedback.get._2
              feedback.cond = currentCond
              feedback.steps = Some(v2)
              Changed(v1.zip(v2).zipWithIndex.map((v, i) => v._1 match
                case IR.Undefined() if v1 != v2 => IR.FeedbackAsk(i, feedback)
                case _ => v._1
              ))
        }
      }

    override val store: Store[IR, IR, WithJoin] = new Store[IR, IR, WithJoin] { // TODO : Store is not supported
      override def read(x: IR): JOption[WithJoin, IR] = ???
      override def write(x: IR, v: IR): Unit = ???
      override def free(x: IR): Unit = ???

      override type State = Unit
      override def getState: State = ()
      override def join: Join[Unit] = implicitly
      override def widen: Widen[Unit] = implicitly
      override def setState(st: Unit): Unit = ()
    }

    override val alloc: Allocator[IR, AllocationSite] =  new AAllocatorFromContext(_ => ???)
    override val print: Print[IR] = new PrintBound
    override val input: UserInput[IR] = new AUserInputFun(IR.Unknown()) with WithNamedUserInput(name => IR.External(name))
    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    override def newInstance: Executor = new Instance(stackConfig)

    class CustomCombinator(phi : Combinator[FixIn, FixOut[IR]]) extends Combinator[FixIn, FixOut[IR]] :
      override def apply(v1: FixIn => FixOut[IR]): FixIn => FixOut[IR] = fixIn => {
        if (currentFeedback.exists(_._1 == fixIn))
          phi(v1)(fixIn)
        else
          val feedback : IR.Feedback = IR.Feedback(
            callFrame.getState,
            None,
            None)

          val beforeFeedback = currentFeedback
          try {
            currentFeedback = Some(fixIn, feedback)
            callFrame.setState(callFrame.getState.zipWithIndex.map((v, i) => v match
              case IR.Undefined() => IR.Undefined()
              case _ => IR.FeedbackAsk(i, feedback)))
            phi(v1)(fixIn)
          }
          finally {
            currentFeedback = beforeFeedback
          }
      }