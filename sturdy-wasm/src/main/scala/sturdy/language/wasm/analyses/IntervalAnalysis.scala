package sturdy.language.wasm.analyses

import cats.effect.ContextShift
import cats.effect.IO
import sturdy.data.{*, given}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.bytememory.IntervalAddressMemory
import sturdy.effect.callframe.{ConcreteCallFrame, JoinableDecidableCallFrame}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.effect.symboltable.IntervalSymbolTable
import sturdy.effect.symboltable.{ConstantSymbolTable, JoinableDecidableSymbolTable}
import sturdy.effect.EffectStack
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.booleans.{*, given}
import sturdy.values.config.BytesSize
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}
import swam.FuncType
import swam.syntax.*
import swam.traversal.Traverser

import java.nio.{ByteBuffer, ByteOrder}
import scala.collection.IndexedSeqView
import WasmFailure.*
import sturdy.control.{ControlObservable, RecordingControlObserver}

object IntervalAnalysis extends Interpreter, IntervalValues, ExceptionByTarget, ControlFlow, Control:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = Seq[NumericInterval[Byte]]
  type Size = Topped[Int]
  type FuncIx = I32
  type FunV = Powerset[FunctionInstance]

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = Convert.apply(v.asInt32, NilCC)
    override def sizeToVal(sz: Size): Value = Value.Int32(Convert.apply(sz, NilCC))

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      val NumericInterval(l, h) = ix.asInt32
      val elems = for (i <- l.max(0) to h.min(vec.size - 1))
        yield vec(i)
      if (elems.isEmpty) {
        // no elems in range
        JOptionPowerset.None()
      } else if (h < vec.size) {
        // all indices in range
        JOptionPowerset.Some(Powerset(elems.toSet))
      } else {
        // some indices in range, but not all
        JOptionPowerset.NoneSome(Powerset(elems.toSet))
      }

    override def invokeHostFunction(hostFunc: HostFunction, args: List[IntervalAnalysis.Value]): List[IntervalAnalysis.Value] = hostFunc match
      case HostFunction.proc_exit =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        eff.joinWithFailure(result)(f.fail(FileError, s"in ${hostFunc.name}"))

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(NumericInterval.constant(i))
      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(NumericInterval.constant(l))
      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(Topped.Actual(f))
      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(Topped.Actual(d))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
      GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc]
//      , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this
    
    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
//    override def widenState: Widen[State] = implicitly

    val rangeLimit = 100
    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: IntervalAddressMemory[MemoryAddr, NumericInterval[Byte]] = new IntervalAddressMemory(NumericInterval(0, 0), rangeLimit)
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val funTable: IntervalSymbolTable[TableAddr, Int, Powerset[FunctionInstance]] = new IntervalSymbolTable(rangeLimit)
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value] = new JoinableDecidableCallFrame(rootFrameData, rootFrameValues.view.zipWithIndex.map(_.swap))
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures
    private given Failure = failure

    given ConvertIntFloat[I32, F32] =
      new TransitiveConvert(using ConvertNumericIntervalToConstant, summon[ConvertIntFloat[Topped[Int], F32]]).adaptConfig(NilCC && _)
    given ConvertIntDouble[I32, F64] =
      new TransitiveConvert(using ConvertNumericIntervalToConstant, summon[ConvertIntDouble[Topped[Int], F64]]).adaptConfig(NilCC && _)
    given ConvertLongFloat[I64, F32] =
      new TransitiveConvert(using ConvertNumericIntervalToConstant, summon[ConvertLongFloat[Topped[Long], F32]]).adaptConfig(NilCC && _)
    given ConvertLongDouble[I64, F64] =
      new TransitiveConvert(using ConvertNumericIntervalToConstant, summon[ConvertLongDouble[Topped[Long], F64]]).adaptConfig(NilCC && _)
    given ConvertFloatInt[F32, I32] =
      new TransitiveConvert(using summon[ConvertFloatInt[F32, Topped[Int]]], ConvertConstantToNumericInterval).adaptConfig(_ && NilCC)
    given ConvertFloatLong[F32, I64] =
      new TransitiveConvert(using summon[ConvertFloatLong[F32, Topped[Long]]], ConvertConstantToNumericInterval).adaptConfig(_ && NilCC)
    given ConvertDoubleInt[F64, I32] =
      new TransitiveConvert(using summon[ConvertDoubleInt[F64, Topped[Int]]], ConvertConstantToNumericInterval).adaptConfig(_ && NilCC)
    given ConvertDoubleLong[F64, I64] =
      new TransitiveConvert(using summon[ConvertDoubleLong[F64, Topped[Long]]], ConvertConstantToNumericInterval).adaptConfig(_ && NilCC)
    given ConvertFloatBytes[F32, Bytes] with
      def apply(from: Topped[Float], conf: BytesSize && SomeCC[ByteOrder]): Seq[NumericInterval[Byte]] =
        val bytes: Seq[Topped[Byte]] = Convert(from, conf)
        bytes.map(Convert.apply(_, NilCC))
    given ConvertBytesFloat[Bytes, F32] with
      override def apply(from: Seq[NumericInterval[Byte]], conf: SomeCC[ByteOrder]): Topped[Float] = {
        val bytes: Seq[Topped[Byte]] = from.map(Convert.apply(_, NilCC))
        Convert(bytes, conf)
      }
    given ConvertDoubleBytes[F64, Bytes] with
      def apply(from: Topped[Double], conf: BytesSize && SomeCC[ByteOrder]): Seq[NumericInterval[Byte]] =
        val bytes: Seq[Topped[Byte]] = Convert(from, conf)
        bytes.map(Convert.apply(_, NilCC))
    given ConvertBytesDouble[Bytes, F64] with
      override def apply(from: Seq[NumericInterval[Byte]], conf: SomeCC[ByteOrder]): Topped[Double] = {
        val bytes: Seq[Topped[Byte]] = from.map(Convert.apply(_, NilCC))
        Convert(bytes, conf)
      }

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    var intIntervalBounds: Set[Int] = Set(-1, 0, 1)
    var longIntervalBounds: Set[Long] = Set(-1, 0, 1)
    given Widen[I32] = new NumericIntervalWiden[Int](intIntervalBounds, Int.MinValue, Int.MaxValue)
    given Widen[I64] = new NumericIntervalWiden[Long](longIntervalBounds, Long.MinValue, Long.MaxValue)
    given Widen[NumericInterval[Byte]] = new NumericIntervalWiden[Byte](Set(), Byte.MinValue, Byte.MaxValue)

    private implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
    private val boundCollector = new Traverser[IO, Unit]() {
      override val i32ConstTraverse = (_, c) => IO(intIntervalBounds += c.v)
      override val i64ConstTraverse = (_, c) => IO(longIntervalBounds += c.v)
    }
    override def initializeModule(module: Module, imports: Imports): ModuleInstance = {
      module.funcs.foreach(f => f.body.foreach(boundCollector.run((), _)))
      module.globals.foreach(g => g.init.foreach(boundCollector.run((), _)))
      super.initializeModule(module, imports)
    }

    val observedConfig = config.withObservers(Seq(this.triggerControlEvent))
    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[IntervalAnalysis.Value]] = new fix.ContextualFixpoint {
      override type Ctx = observedConfig.ctx.Ctx
      val (contextPreparation, sensitivity) = observedConfig.ctx.make[IntervalAnalysis.Value]
      import observedConfig.ctx.finiteCtx
      override protected def contextFree = phi =>
        fix.log(controlEventLogger(Instance.this, effectStack, except), contextPreparation(phi))
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = observedConfig.fix.get
    }

    override def toString: String = s"constant $config"
