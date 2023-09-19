package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.TopMemory
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.{JoinableDecidableSymbolTable, UpperBoundSymbolTable}
import sturdy.fix
import sturdy.fix.Combinator
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.values.floating.FloatOps
import swam.syntax.*
import swam.FuncType
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.types.{*, given}
import sturdy.values.{*, given}
import sturdy.values.references.{ReferenceOps, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView
import WasmFailure.*

object TypeAnalysis extends Interpreter, TypeValues, ExceptionByTarget, ControlFlow:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = BaseType[Seq[Byte]]
  type Size = I32
  type FuncIx = I32
  type FunV = Powerset[FunctionInstance]
  type FuncRef = Powerset[FunctionInstance]

  given TypeSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Size, FuncIx, FunV, FuncRef, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Num(NumValue.Int32(sz))
    override def intToVal(i: Int): Value = Value.Num(NumValue.Int32(sturdy.language.wasm.analyses.TypeAnalysis.topI32))
    override def numToRef(v: Value): Value = ???
    override def valToInt(v: Value): Int = ???
    override def funcRefToInt(r: Powerset[FunctionInstance]): Int = ???
    override def funcRefToVal(r: Powerset[FunctionInstance]): TypeAnalysis.Value = ???
    override def valToFuncRef(v: TypeAnalysis.Value): Powerset[FunctionInstance] = ???
    override def funcInstToFuncRef(f: FunctionInstance): Powerset[FunctionInstance] = ???
    override def funcInstToFunV(f: FunctionInstance): Powerset[FunctionInstance] = ???
    override def funVToFuncRef(f: Powerset[FunctionInstance]): Powerset[FunctionInstance] = ???
    override def makeNullRef: TypeAnalysis.Value = ???
    override def makeNullFuncRef: Powerset[FunctionInstance] = ???
    override def makeExternNullRef: TypeAnalysis.Value = ???
    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      if (vec.isEmpty)
        JOptionPowerset.None()
      else
        JOptionPowerset.NoneSome(Powerset(vec.toSet))

    override def invokeHostFunction(hostFunc: HostFunction, args: List[TypeAnalysis.Value]): List[TypeAnalysis.Value] = hostFunc match
      case HostFunction.proc_exit =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        eff.joinWithFailure(result)(f.fail(FileError, s"in ${hostFunc.name}"))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
    GenericInstance
//    , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this

    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[Value]] = new fix.ContextualFixpoint {
      override type Ctx = config.ctx.Ctx
      val (contextPreparation, sensitivity) = config.ctx.make[Value]
      import config.ctx.finiteCtx
      override protected def contextFree = contextPreparation
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = config.fix.get
    }

    override val fixpointSuper = fixpoint

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly

    override def jvFuncRef: WithJoin[Powerset[FunctionInstance]] = implicitly

    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: TopMemory[MemoryAddr, Addr, Bytes, Size] = new TopMemory
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val tables: UpperBoundSymbolTable[TableAddr, FuncIx, FuncRef] = new UpperBoundSymbolTable(Powerset())
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value] = new JoinableDecidableCallFrame(rootFrameData, rootFrameValues.view.zipWithIndex.map(_.swap))
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures
    override var tableLimits: List[(Int, Option[Int])] = List()
    given Failure = failure

    implicit val z: ReferenceOps[FunV, FuncRef] = implicitly
    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, FuncRef, WithJoin] = implicitly

    override def toString: String = s"type $config"
