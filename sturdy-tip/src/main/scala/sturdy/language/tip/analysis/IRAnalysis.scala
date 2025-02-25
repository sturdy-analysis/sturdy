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

    given Join[IR] = (v1: IR, v2: IR) => currentCond match
      case None => Changed(IR.Join(v1, v2))
      case Some(cond) => Changed(IR.Select(cond, v2, v1))

    given Widen[IR] = (v1: IR, v2: IR) => // TODO : Fix this
      println(v1)
      println(v2)
      Unchanged(v1)

    override def jv: WithJoin[IR] = implicitly

    override val fixpoint =
      notContextSensitive[FixIn, FixOut[IR], Combinator[FixIn, FixOut[IR]]](
        fix.dispatch(isFunOrWhile, Seq(
          fix.iter.innermost(stackConfig), fix.iter.innermost(stackConfig)
        ))
      ).fixpoint

    override val intOps: IntegerOps[Int, IR] = implicitly
    override val boolOps: BooleanOps[IR] = implicitly
    override val compareOps: OrderingOps[IR, IR] = implicitly
    override val eqOps: EqOps[IR, IR] = implicitly
    override val functionOps: FunctionOps[Function, Seq[IR], IR, IR] = new IRFunctionOps[Function, Seq[IR]](identity)

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
          joinComputations(thn)(els)
        }
        finally {
          currentCond = condBefore
        }
    }

    override val callFrame: JoinableDecidableCallFrame[String, String, IR, Exp.Call] =
      new JoinableDecidableCallFrame[String, String, IR, Exp.Call]("$main", Iterable.empty)

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