package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.{ConcreteCallFrame, JoinableConcreteCallFrame}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableConcreteOperandStack, given}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.effect.symboltable.{ConstantSymbolTable, JoinableConcreteSymbolTable}
import sturdy.effect.{AnalysisState, EffectStack}
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{config, *, given}
import swam.FuncType
import swam.syntax.*

import java.nio.{ByteBuffer, ByteOrder}
import scala.collection.IndexedSeqView

object IntervalAnalysis extends Interpreter, IntervalValues, ExceptionByTarget, ControlFlow:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = Seq[Topped[Byte]]
  type Size = I32
  type FuncIx = I32
  type FunV = Powerset[FunctionInstance]

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      ix.asInt32 match
        case NumericInterval.Top() =>
          if (vec.isEmpty)
            JOptionPowerset.None()
          else
            JOptionPowerset.NoneSome(Powerset(vec.toSet))
        case NumericInterval.Bounded(l, h) =>
          val elems = for (i <- l.min(0) until h.max(vec.size))
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
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        eff.joinWithFailure(result)(f.fail(FileError, s"in ${hostFunc.name}"))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
      GenericInstance
//      , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this
    
    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
//    override def widenState: Widen[State] = implicitly

    
    val stack: JoinableConcreteOperandStack[Value] = new JoinableConcreteOperandStack
    val memory: ConstantAddressMemory[MemoryAddr, Topped[Byte]] = new ConstantAddressMemory(Topped.Actual(0))
    val globals: JoinableConcreteSymbolTable[Unit, GlobalAddr, Value] = new JoinableConcreteSymbolTable
    val funTable: ConstantSymbolTable[TableAddr, Int, Powerset[FunctionInstance]] = new ConstantSymbolTable
    val callFrame: JoinableConcreteCallFrame[FrameData, Int, Value] = new JoinableConcreteCallFrame(rootFrameData, rootFrameValues.view.zipWithIndex.map(_.swap))
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: AFailureCollect = new AFailureCollect
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

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[IntervalAnalysis.Value]] = new fix.ContextualFixpoint {
      override type Ctx = config.ctx.Ctx
      val (contextPreparation, sensitivity) = config.ctx.make[IntervalAnalysis.Value]
      import config.ctx.finiteCtx
      override protected def contextFree = contextPreparation
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = config.fix.get(using analysisState, effectStack)
    }

    override val fixpointSuper = fixpoint
    override def toString: String = s"constant $config"
