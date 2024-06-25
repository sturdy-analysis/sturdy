package sturdy.language.pcf

import sturdy.data.{*, given}
import sturdy.apron.{*, given}
import apron.*
import sturdy.values.booleans.{*, given}
import sturdy.values.closures.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.types.{*, given}
import sturdy.values.{*, given}
import sturdy.effect.environment.{*, given}
import sturdy.effect.failure.{*, given}
import sturdy.effect.userinput.{*, given}
import sturdy.effect.store.{*, given}
import sturdy.effect.allocation.{*, given}
import sturdy.fix.{*, given}
import sturdy.language.pcf.SummaryBasedCFA.Type
import sturdy.util.{*, given}
import sturdy.values

import scala.collection.mutable


object SummaryBasedCFA:
  final type J[A] = WithJoin[A]

  enum Value:
    case Int(i: VInt)
    case Closure(closure: VClosure)
    case UnknownType(i: PowVirtAddr)

  given CombineValue[W <: Widening](using Combine[VInt, W], Combine[VClosure, W], Combine[PowVirtAddr, W]): Combine[Value, W] = {
    case (Value.Int(i1), Value.Int(i2)) => Combine(i1, i2).map(Value.Int(_))
    case (Value.Closure(cls1), Value.Closure(cls2)) => Combine(cls1, cls2).map(Value.Closure(_))
    case (Value.UnknownType(addr1), Value.UnknownType(addr2)) => Combine(addr1, addr2).map(Value.UnknownType(_))
  }

  enum Addr:
    case Param(name: String)
    case Local(name: String)
    case Temp(exp: Exp)

  import Addr.*

  given Finite[Addr] with {}

  given Ordering[Addr] = {
    case (Param(n1), Param(n2)) => n1.compareTo(n2)
    case (Local(n1), Local(n2)) => n1.compareTo(n2)
    case (Temp(e1), Temp(e2)) => Ordering[Label].compare(e1.label, e2.label)
    case (Param(_), Local(_)) => 1
    case (Param(_), Temp(_)) => 1
    case (Local(_), Temp(_)) => 1
    case (Temp(_), Local(_)) => -1
    case (Temp(_), Param(_)) => -1
    case (Local(_), Param(_)) => -1
  }

  type VirtAddr = VirtualAddress[Addr]
  type PhysAddr = PhysicalAddress[Addr]
  type PowVirtAddr = PowVirtualAddress[Addr]
  type PowPhysAddr = PowersetAddr[PhysAddr,PhysAddr]
  type ApronExprPhysAddr = ApronExpr[PhysAddr, Type]

  enum Type:
    case IntType

  import Type.*

  given ApronType[Type] with
    extension (t: Type)
      def apronRepresentation: ApronRepresentation = ApronRepresentation.Int
      override def roundingDir: RoundingDir = RoundingDir.Zero
      override def roundingType: RoundingType = RoundingType.Int
      override def byteSize: Int = Integer.BYTES

  given Finite[Type] with {}
  given Join[Type] = {
    case (t1@IntType, IntType) => Unchanged(t1)
  }
  given Widen[Type] = implicitly


  override type VInt = ApronExpr[VirtAddr, Type]
  override type VBoolean = ApronCons[VirtAddr,Type]
  override type VClosure = Powerset[Closure[String, Exp, Env]]
  override type Env = Map[String, Unit]

  given Finite[Closure[String, Exp, Env]] with {}

  class Instance(nextInput: () => Value) extends GenericInterpreter[Value, Env, J]:
    given Instance = this

    extension(value: Value)
      def asInt(using failure: Failure): VInt = value match
        case Value.Int(i) => i
        case Value.UnknownType(addr) =>
          val res = recencyStore.read(addr)
          res match
            case Value.Int(i) => i
            case _ => failure(TypeError, s"Expected Int but got $res")
        case _ => failure(TypeError, s"Expected Int but got $value")

      def asClosure(using Failure): VClosure = value match
        case Value.Closure(closure) => closure
        case Value.UnknownType(addr) =>
          val res = recencyStore.read(addr)
          res match
            case Value.Closure(closure) => closure
            case _ => failure(TypeError, s"Expected Int but got $res")
        case _ => Failure(TypeError, s"Expected Closure but got $value")


      def asBoolean(v: Value)(using Failure): VBoolean = ???

      def boolean(b: VBoolean): Value = ???


    implicit val tempRelationalAlloc: AAllocatorFromContext[Type, Addr] = AAllocatorFromContext(_ => Addr.Temp(domLogger.currentDom.get))
    implicit val localRelationaAlloc: AAllocatorFromContext[String, Addr] = AAllocatorFromContext(Addr.Local(_))

    given apronManager: Manager = new apron.Polka(true)

    var exprConverter: ApronExprConverter[Addr, Type, Value] = null
    var apronState: ApronRecencyState[Addr, Type, Value] = null
    given lazyApronState: Lazy[ApronState[VirtAddr, Type]] = lazily(apronState)
    val relationalStore: RelationalStore[Addr, Type, PowPhysAddr, Value] = new RelationalStore[Addr, Type, PowPhysAddr, Value] (
      manager = apronManager,
      initialState = apron.Abstract1(apronManager, new apron.Environment()),
      initialTypeEnv = Map()
    ):
      override def getRelationalVal(v: Value): Option[ApronExprPhysAddr] =
        v match
          case Value.Int(iv) => Some(exprConverter.virtToPhys(iv))
          case _ => None

      override def makeRelationalVal(expr: ApronExprPhysAddr): Value =
        Value.Int(exprConverter.physToVirt(expr))

    val recencyStore: RecencyStore[Addr, PowVirtAddr, Value] = new RecencyStore(relationalStore)
    exprConverter = ApronExprConverter(recencyStore, relationalStore)
    apronState = new ApronRecencyState[Addr, Type, Value](tempRelationalAlloc, recencyStore, relationalStore)
    given ApronState[VirtualAddress[Addr], Type] = apronState

    override def jv: WithJoin[Value] = implicitly

    override val failure: ConcreteFailure = new ConcreteFailure
    given Failure = failure

    given IntegerOps[Int, Type] = LiftedIntegerOps[Int, Type, BaseType[Int]](extract = { case IntType => BaseType[Int] }, inject = _ => IntType)
    override val intOps: IntegerOps[Int, Value] = implicitly
    override val eqOps: EqOps[Value, Value] = implicitly
    override val orderingOps: OrderingOps[Value, Value] = implicitly
    override val branchOps: BooleanBranching[Value, Value] = implicitly

    given ClosureOps[String, Exp, Env, Value, VClosure] with
      case class Summary(retEnv: environment.State, retVal: Value)
      val summaries: mutable.Map[Closure[String, Exp, Env], Summary] = mutable.Map()
      override def closureValue(params: String, body: Exp, env: Env): VClosure =
        Powerset(Closure(params, body, env))

      override def invokeClosure(powClosure: VClosure, args: Value)(invoke: Exp => Value): Value =
        powClosure.foreachJoin {
          closure => invokeClosure(closure, args)(invoke)
        }
      def invokeClosure(closure: Closure[String, Exp, Env], args: Value)(invoke: Exp => Value): Value =
        summaries.get(closure) match
          case None =>
            val summary = computeSummary(closure)(invoke)
            summaries.put(closure, summary)
            applySummary(summary, args)
          case Some(summary) =>
            applySummary(summary, args)

      def applySummary(summary: Summary, args: Value): Value = ???
      def computeSummary(closure: Closure[String, Exp, Env])(invoke: Exp => Value): Summary =
        environment.scoped {
          environment.loadClosedEnvironment(closure.env)
          val paramAddr = recencyStore.alloc(Param(closure.params))
          environment.bind(closure.params, Value.UnknownType(PowVirtualAddress(paramAddr)))
          val retVal = invoke(closure.body)
          val state = environment.getState
        }


    override val closureOps: ClosureOps[String, Exp, Env, Value, Value] = implicitly

    override val input: UserInput[Value] = new AUserInputFun[Value](approx = {
      val addr = tempRelationalAlloc.alloc(Type.IntType)
      val vaddr = recencyStore.alloc(addr)
      Value.Int(ApronExpr.addr(vaddr, Type.IntType))
    })
    override val environment: CyclicEnvironment[String, Value, J] with ClosableEnvironment[String, Value, Env, J] = ???

    val domLogger: DomLogger[Exp, Value] = new DomLogger

    override val fixpoint = ???