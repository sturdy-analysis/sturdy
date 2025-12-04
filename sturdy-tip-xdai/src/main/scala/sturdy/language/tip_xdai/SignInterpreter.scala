package sturdy.language.tip_xdai

import sturdy.data.MayJoin.WithJoin
import sturdy.effect.allocation.{AAllocatorFromContext, CAllocatorIntIncrement}
import sturdy.effect.failure.CollectedFailures
import sturdy.effect.store.AStoreThreaded
import sturdy.language.tip_xdai.arithmetic.sign.{SignIntValue, SignEqOps as ArithmeticSignEqOps, SignInterpreter as ArithmeticSignInterpreter, SignJoin as ArithmeticSignJoin}
import sturdy.language.tip_xdai.core.abstractions.{FiniteValue, TopValue}
import sturdy.language.tip_xdai.core.sign.{PSetFunValue, SignEqOps as CoreSignEqOps, SignJoin as CoreSignJoin}
import sturdy.language.tip_xdai.core.abstractions.BoolValue
import sturdy.language.tip_xdai.core.{Call, CoreGenericInterpreter, FixIn, FixOut, Function, TipFailure, Value, While, given_Finite_TipFailure}
import sturdy.language.tip_xdai.record.sign.{SignEqOps as RecordSignEqOps, SignInterpreter as RecordSignInterpreter, SignJoin as RecordSignJoin}
import sturdy.language.tip_xdai.references.sign.{AbstractSignAddr, SignEqOps as ReferencesSignEqOps, SignInterpreter as ReferencesSignInterpreter, SignJoin as ReferencesSignJoin}
import sturdy.values.{Finite, Join, Powerset, Topped}
import sturdy.values.booleans.{BooleanBranching, LiftedBooleanBranching}
import sturdy.values.functions.{FunctionOps, LiftedFunctionOps}
import sturdy.values.ordering.EqOps
import sturdy.language.tip_xdai.references.{AllocSite, AllocationSite}
import sturdy.language.tip_xdai.record.RecordSite
import sturdy.values.booleans.{ConcreteBooleanBranching, ToppedBooleanBranching}
import sturdy.values.references.{AllocationSiteAddr, PowersetAddr}
import sturdy.data.CombineUnit
import sturdy.effect.callframe.{DecidableCallFrame, JoinableDecidableCallFrame}
import sturdy.effect.print.PrintFiniteAlphabet
import sturdy.effect.userinput.AUserInput
import sturdy.fix
import sturdy.fix.{CombinatorFixpoint, StackConfig}
import sturdy.util.{Lazy, lazily}
import sturdy.values.integer.IntSign
import sturdy.data.noJoin
import sturdy.values.functions.PowersetFunctionOps
import sturdy.values.functions.ConcreteFunctionOps
import sturdy.data.MakeJoined
import sturdy.effect.EffectStack
import sturdy.values.finitely
import sturdy.values.references.given_Finite_AllocationSiteAddr
import sturdy.fix.context.FiniteParameters
import sturdy.language.tip_xdai.core.finiteFixIn
import sturdy.data.finiteUnit
import sturdy.language.tip_xdai.core.CombineFixOut
import sturdy.values.Topped.Top

// TODO: Support observer

class SignInterpreter extends CoreGenericInterpreter[Value, WithJoin]
  with ArithmeticSignInterpreter
  with RecordSignInterpreter
  with ReferencesSignInterpreter:

  class SignEqOps extends CoreSignEqOps
    with ArithmeticSignEqOps
    with RecordSignEqOps
    with ReferencesSignEqOps:

    override def boolToValue(b: Topped[Boolean]): Value = b match
      case Topped.Top => TopValue
      case Topped.Actual(b) => BoolValue(b)

  class SignJoin extends CoreSignJoin
    with ArithmeticSignJoin
    with RecordSignJoin
    with ReferencesSignJoin

  given Join[Value] = new SignJoin

  override def jv: WithJoin[Value] = implicitly


  override lazy val failure: CollectedFailures[TipFailure] = new CollectedFailures

  override val eqOps: EqOps[Value, Value] = new SignEqOps

  override val functionOps: FunctionOps[Function, Seq[Value], Value, Value] = new LiftedFunctionOps[Function, Seq[Value], Value, Value, Powerset[Function]](
    {
      case PSetFunValue(f) => f
      case TopValue => Powerset(getFunctions.toSet)
    },
    {
      case Powerset(s) if s == getFunctions.toSet => TopValue
      case ps => PSetFunValue(ps)
    }
  )

  override val branchOps: BooleanBranching[Value, Unit] = new LiftedBooleanBranching[Value, Topped[Boolean], Unit](
    {
      case BoolValue(b) => Topped.Actual(b)
      case _ => Topped.Top
    }
  )

  // References & Records
  override val store: AStoreThreaded[AllocationSiteAddr, AbstractSignAddr, Value] = new AStoreThreaded(Map.empty)

  def extractAllocationSiteAddr(allocationSite: AllocationSite): AllocationSiteAddr = allocationSite match
    case AllocSite(e) => AllocationSiteAddr.Alloc(e.label)(true)
    case RecordSite(r) => AllocationSiteAddr.Alloc(r.label)(true)

  override val alloc: AAllocatorFromContext[AllocationSite, AbstractSignAddr] = new AAllocatorFromContext(site =>
    PowersetAddr(extractAllocationSiteAddr(site))
  )

  override val callFrame: JoinableDecidableCallFrame[String, String, Value, Call] = new JoinableDecidableCallFrame("$main", Iterable.empty)
  override val print: PrintFiniteAlphabet[Value] = new PrintFiniteAlphabet
  override val input: AUserInput[Value] = new AUserInput(TopValue)

  given Lazy[Finite[Value]] = lazily(FiniteValue)

  val stackConfig = StackConfig.StackedStates()
  val observedStackConfig = stackConfig//.withObservers(Seq(this.triggerControlEvent))

  def isFunOrWhile(dom: FixIn): Int = dom match
    case FixIn.EnterFunction(_) => 0
    case FixIn.Run(While(_, _)) => 1
    case _ => -1

  final def parameters(callFrame: DecidableCallFrame[String, String, Value, Call]): fix.context.Sensitivity[FixIn, fix.context.Parameters[String, Value]] =
    fix.context.parameters[FixIn, String, Value] {
      case FixIn.EnterFunction(f) => Some(f.params.map(x => x -> callFrame.getLocalByName(x).getOrElse(null)).toMap)
      case _ => None
    }

  def parameterSensitive(phi: (fix.Contextual[fix.context.Parameters[String, Value], FixIn, FixOut[Value]]) ?=> fix.Combinator[FixIn, FixOut[Value]]): fix.Combinator[FixIn, FixOut[Value]] =
    fix.contextSensitive(parameters(callFrame), phi)

  override val fixpoint: EffectStack ?=> CombinatorFixpoint[FixIn, FixOut[Value]] =
    //fix.log(controlEventLogger(this),
    fix.filter((dom: FixIn) => isFunOrWhile(dom) >= 0,
      parameterSensitive(
        fix.iter.innermost(observedStackConfig)
      )
    ).fixpoint


