package sturdy.language.tip.backward

import sturdy.data.{WithJoin, given}
import sturdy.effect.allocation.AAllocationFromContext
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.failure.{CollectedFailures, Failure}
import sturdy.effect.{EffectStack, TrySturdy, given}
import sturdy.effect.print.{PrintFiniteAlphabet, given}

import sturdy.effect.store.must.PowersetAddrMustStore
import sturdy.effect.store.Store

import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.context.FiniteParameters
import sturdy.fix.{CombinatorFixpoint, StackConfig, given}
import sturdy.language.tip.{AllocationSite, Field, *, given}
import sturdy.language.tip.backward.abstractions.*
import sturdy.language.tip.backward.values.{*, given}
import sturdy.util.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}

object IntervalBackwardAnalysis extends BackwardsInterpreter, References.AllocationSites, Ints.MInterval, Functions.Powerset, Records.PreciseFieldsOrTop, Fix:

  given Lazy[Join[Value]] = lazily(CombineValue)

  //  var bounds: Set[Double] = Set()
  //  given Widen[Interval] = new IntervalWiden(bounds, Double.MinValue, Double.MaxValue)
  //  given Lazy[Widen[Value]] = lazily(CombineValue[Widening.Yes])

  class Instance(initEnvironment: Environment, initStore: Store, stackConfig: StackConfig) extends GenericBackwardsInstance:
    override def jv: WithJoin[Value] = implicitly
    override val meet: Meet[Value] = implicitly

    override val failure: CollectedFailures[TipFailure] = new CollectedFailures
    private given Failure = failure

    override val topInt: Value = Value.IntValue(Interval.I(1,1))
    override def topFunction: Value = Value.FunValue(Powerset.apply(functions.values.toSet))

    override def topAddr: Powerset[AllocationSiteAddr] =
      Powerset(functions.values.toSet.flatMap(_.fold(using
        fun => (fun.params ++ fun.locals).map(p => AllocationSiteAddr.Variable(s"${fun.name}:$p")(true)).toSet,
        _ => Set[AllocationSiteAddr](),
        { case Exp.Alloc(e) => Set(AllocationSiteAddr.Alloc(e.label)(true)); case _ => Set() }
      )))

    given Lazy[EqOps[Value, Value]] = lazily(eqOps)
    override val intOps: BackIntegerOps[Int, Value] = implicitly
    override val compareOps: BackOrderingOps[Value, Value] = implicitly

    override val backEqOps: BackEqOps[Value, Value] = new BackEqOps[Value, Value]:
      override def equ(v1: Value => Value, v2: Value => Value, r: Value): Value = asBoolean(r) match
        case Topped.Top => v2(Value.TopValue); v1(Value.TopValue); r
        case Topped.Actual(true) => v1(v2(Value.TopValue)); r
        case Topped.Actual(false) => v2(Value.TopValue) match
          case Value.IntValue(Interval.I(0,0)) =>
            effectStack.joinComputations {
              v1(Value.IntValue(Interval.I(Int.MinValue,-1)))//IntSign.Neg? what is going on here???
            } {
              v1(Value.IntValue(Interval.I(1,Int.MaxValue))) //IntSign.Pos
            }
            r
          case _ => v1(Value.TopValue); r

      override def neq(v1: Value => Value, v2: Value => Value, r: Value): Value = asBoolean(r) match
        case Topped.Top => v2(Value.TopValue); v1(Value.TopValue); r
        case Topped.Actual(b) => equ(v1, v2, boolean(Topped.Actual(!b))); r

    override val eqOps: EqOps[Value, Value] = implicitly
    override val functionOps: BackFunctionOps[Function, Seq[Value], Value, Value] = implicitly
    override val refOps: ReferenceOps[Addr, Value] = implicitly
    override val recOps: RecordOps[Field, Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Unit] = implicitly

    override val callFrame: JoinableDecidableCallFrame[Unit, String, Value] = new JoinableDecidableCallFrame((), initEnvironment)
    override val store: PowersetAddrMustStore[AllocationSiteAddr, Value] = new PowersetAddrMustStore(initStore)
    override val alloc: AAllocationFromContext[AllocationSite, Addr] = new AAllocationFromContext(fromAllocationSite)
    override val print: AUserInput[Value] = new AUserInput(Value.IntValue(Interval.ITop))
    override val input: PrintFiniteAlphabet[Value] = new PrintFiniteAlphabet



    given Lazy[Finite[Value]] = lazily(FiniteValue)

    def getState = this.effectStack.getAllState.head

    val logger = new fix.Logger[BackFixIn[Value], BackFixOut[Value]]:
      override def enter(dom: BackFixIn[IntervalBackwardAnalysis.Value]): Unit = dom match
        case BackFixIn.EnterFunction(f, v) => println(s"Postcondition of $f = $v\n\t$getState")
        case _ => //nothing

      override def exit(dom: BackFixIn[IntervalBackwardAnalysis.Value], codom: TrySturdy[BackFixOut[IntervalBackwardAnalysis.Value]]): Unit = (dom, codom.get) match
        case (BackFixIn.Run(s), Some(BackFixOut.Run())) if !s.isInstanceOf[Stm.Block] =>
          println(s"Precondition of $s\n\t${getState}")
        case (BackFixIn.Run(s), None) => println(s"Precondition of $s = bottom")
        case (BackFixIn.EnterFunction(f, v), _) => println(s"Precondition of $f = $v\n\t${getState}")
        case _ => // nothing

    override val fixpoint: EffectStack ?=> fix.Fixpoint[BackFixIn[Value], BackFixOut[Value]] =
      fix.log(logger,
        fix.filter((dom: BackFixIn[Value]) => isFunOrWhile(dom) >= 0,
          fix.notContextSensitive(
            fix.iter.innermost[BackFixIn[Value], BackFixOut[Value], Unit](stackConfig)))).fixpoint
    override def newInstance: sturdy.Executor = new Instance(initEnvironment, initStore, stackConfig)
