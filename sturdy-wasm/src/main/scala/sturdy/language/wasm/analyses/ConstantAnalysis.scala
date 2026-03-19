package sturdy.language.wasm.analyses

import sturdy.control.{ControlEvent, ControlObservable}
import sturdy.data.{*, given}
import sturdy.values.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.symboltable.{ConstantIntervalMappedSymbolTable, ConstantSymbolTable, FiniteSymbolTableWithDrop, IntervalMappedSymbolTable, JoinableDecidableSymbolTable, SizedConstantTable, SizedSymbolTable, SymbolTable, SymbolTableWithDrop, joinLimit}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.JoinableDecidableOperandStack
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Control.Exc
import sturdy.language.wasm.abstractions.Fix.given
import sturdy.language.wasm.generic.WasmFailure.*
import sturdy.language.wasm.generic.{*, given}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.addresses.AddressOffset
import sturdy.values.booleans.given
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.given
import sturdy.values.floating.{*, given}
import sturdy.values.functions.given
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.given
import sturdy.values.references.given
import sturdy.values.simd.given
import sturdy.values.{*, given}
import swam.ReferenceType
import swam.ReferenceType.{ExternRef, FuncRef}
import swam.syntax.*

object ConstantAnalysis extends Interpreter, ConstantValues, ExceptionByTarget, ControlFlow, Control:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = Seq[Topped[Byte]]
  type Size = I32
  type Index = I32

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Bytes, Size, Index, FunV, RefV, WithJoin] with
    override def valToAddr(v: Value): Addr = v.asInt32
    override def valToIdx(v: Value): Index = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Num(NumValue.Int32(sz))
    override def valToRef(v: ConstantAnalysis.Value, funcs: Vector[FunctionInstance]): RefV =
      v match
        case Value.Ref(f) => f
        case Value.TopValue =>
          Powerset[FunctionInstance | ExternReference](funcs*) ++ Powerset[FunctionInstance | ExternReference](ExternReference.ExternReference, ExternReference.Null)
        case _ => f.fail(TypeError, s"Expected reference, but got $v")

    override def refToVal(r: RefV): Value = Value.Ref(r)

    override def liftBytes(b: Seq[Byte]): Seq[Topped[Byte]] = b.map(Topped.Actual(_))

    override def funcInstToRefV(f: FunctionInstance): RefV = Powerset[FunctionInstance | ExternReference](f)

    override def wrapExnRef(e: ExceptionInstance[Value]): Value = Value.Ref(e)
    override def unwrapExnRef(v: Value): ExceptionInstance[Value] = v match
      case Value.Ref(e: ExceptionInstance[Value]) => e
      case _ => f.fail(TypeError, s"Expected exnref but got $v")

    override def isNullRef(r: Value): ConstantAnalysis.Value = {
      r match {
        case Value.Ref(_: ExceptionInstance[?]) =>
          makeI32(Topped.Actual(0))
        case Value.Ref(f) =>
          val ps = f.asInstanceOf[Powerset[FunctionInstance | ExternReference]]
          if (ps.set.contains(ExternReference.Null) || ps.set.contains(FunctionInstance.Null))
            if (ps.set.filter(_ == ExternReference.Null) ++ ps.set.filter(_ == FunctionInstance.Null) == ps.set)
              makeI32(Topped.Actual(1))
            else
              makeI32(Topped.Top)
          else
            makeI32(Topped.Actual(0))
        case ConstantAnalysis.Value.TopValue => makeI32(Topped.Top)
        case _ => Value.Num(NumValue.Int32(Topped.Actual(0)))
      }
    }

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      ix.asInt32 match
        case Topped.Actual(i) =>
          if (i >= 0 && i < vec.size)
            JOptionPowerset.Some(Powerset(vec(i)))
          else
            JOptionPowerset.None()
        case Topped.Top =>
          if (vec.isEmpty)
            JOptionPowerset.None()
          else
            JOptionPowerset.NoneSome(Powerset(vec.toSet))

  given ConstantAddressOffset(using f: Failure, effectStack: EffectStack): AddressOffset[Addr] with
    override def addOffsetToAddr(offset: Int, addr: Topped[Int]): Topped[Int] =
      addr match
        case Topped.Top =>
          effectStack.joinWithFailure {
              Topped.Top
          } {
            f.fail(MemoryAccessOutOfBounds, s"$addr + $offset")
          }
        case Topped.Actual(a) =>
          val resultAddr = a + offset
          if (Integer.compareUnsigned(resultAddr, offset) < 0) {
            f.fail(MemoryAccessOutOfBounds, s"$addr + $offset")
          } else {
            Topped.Actual(resultAddr)
          }

    override def moveAddress(addr: Topped[Int], srcOffset: Topped[Int], dstOffset: Topped[Int]): Topped[Int] =
      addr.binary(_ - _, srcOffset).binary(_ + _, dstOffset)

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(i)) => Value.Num(NumValue.Int32(Topped.Actual(i)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(l)) => Value.Num(NumValue.Int64(Topped.Actual(l)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => Value.Num(NumValue.Float32(Topped.Actual(f)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => Value.Num(NumValue.Float64(Topped.Actual(d)))
      case ConcreteInterpreter.Value.Ref(r: FunctionInstance) => Value.Ref(Powerset(r))
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.ExternReference.Null) => Value.Ref(Powerset(ExternReference.Null))
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.ExternReference.ExternReference) => Value.Ref(Powerset(ExternReference.ExternReference))
      case ConcreteInterpreter.Value.Vec(v) => Value.Vec(Topped.Actual(v))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:
    private given Instance = this

    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvBytes: WithJoin[Bytes] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
    override def jvRefV: WithJoin[RefV] = implicitly
    override def jvElem: WithJoin[Elem] = implicitly
//    override def widenState: Widen[State] = implicitly

    given GaloisConnection[Int, Value] with {
      def asAbstract(a: Int): Value = Value.Num(NumValue.Int32(Topped.Actual(a)))
      def concretize(b: Value)(using f: Failure): Int = b match {
        case Value.Num(NumValue.Int32(Topped.Actual(i))) => i
        case Value.TopValue => f.fail(ConversionFailure, s"Cannot unapply $b to Int")
        case _ => f.fail(ConversionFailure, s"Cannot unapply $b to Int")
      }
    }

    given GaloisConnection[Long, Value] with {
      def asAbstract(a: Long): Value = Value.Num(NumValue.Int64(Topped.Actual(a)))
      def concretize(b: Value)(using f: Failure): Long = b match {
        case Value.Num(NumValue.Int64(Topped.Actual(l))) => l
        case Value.TopValue => f.fail(ConversionFailure, s"Cannot unapply $b to Long")
        case _ => f.fail(ConversionFailure, s"Cannot unapply $b to Long")
      }
    }

    given GaloisConnection[Float, Value] with {
      def asAbstract(a: Float): Value = Value.Num(NumValue.Float32(Topped.Actual(a)))
      def concretize(b: Value)(using f: Failure): Float = b match {
        case Value.Num(NumValue.Float32(Topped.Actual(f))) => f
        case Value.TopValue => f.fail(ConversionFailure, s"Cannot unapply $b to Float")
        case _ => f.fail(ConversionFailure, s"Cannot unapply $b to Float")
      }
    }

    given GaloisConnection[Double, Value] with {
      def asAbstract(a: Double): Value = Value.Num(NumValue.Float64(Topped.Actual(a)))
      def concretize(b: Value)(using f: Failure): Double = b match {
        case Value.Num(NumValue.Float64(Topped.Actual(d))) => d
        case Value.TopValue => f.fail(ConversionFailure, s"Cannot unapply $b to Double")
        case _ => f.fail(ConversionFailure, s"Cannot unapply $b to Double")
      }
    }

    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: ConstantAddressMemory[MemoryAddr, Topped[Byte]] = new ConstantAddressMemory(Topped.Actual(0))
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val elems: SymbolTableWithDrop[Unit, ElemAddr, Elem, J] = FiniteSymbolTableWithDrop[Unit, ElemAddr, Elem](Seq.empty[RefV])(using CombineEquiSeq, CombineEquiSeq, implicitly, implicitly)
    val tables: ConstantIntervalMappedSymbolTable[TableAddr, RefV] = new ConstantIntervalMappedSymbolTable[TableAddr, RefV]
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value, InstLoc] = new JoinableDecidableCallFrame(FrameData.empty, Iterable.empty)
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, RefV, WithJoin] = implicitly

    override def invokeHostFunction(hostFunc: HostFunction, args: List[ConstantAnalysis.Value]): List[ConstantAnalysis.Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        failure.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        effectStack.joinWithFailure(result)(failure.fail(FileError, s"in ${hostFunc.name}"))

    val observedConfig = config.withObservers(Seq(this.triggerControlEvent))
    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[ConstantAnalysis.Value]] = new fix.ContextualFixpoint {
      override type Ctx = observedConfig.ctx.Ctx
      val (contextPreparation, sensitivity) = observedConfig.ctx.make[ConstantAnalysis.Value]
      import observedConfig.ctx.finiteCtx
      override protected def contextFree = phi =>
        fix.log(controlEventLogger(Instance.this, effectStack, except), contextPreparation(phi))
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = observedConfig.fix.get
    }

    override def toString: String = s"constant $config"
