package sturdy.language.wasm

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.ConcreteMemory
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.ConcreteExcept
import sturdy.effect.failure.Failure
import sturdy.effect.failure.CFailure
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.symboltable.ConcreteSymbolTable
import sturdy.fix
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.*
import sturdy.values.booleans.BooleanBranching
import sturdy.values.floating.FloatOps
import swam.syntax.*
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.*
import sturdy.values.exceptions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.relational.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder

object ConcreteInterpreter extends Interpreter:
  override type J[A] = NoJoin[A]
  override type I32 = Int
  override type I64 = Long
  override type F32 = Float
  override type F64 = Double
  override type Bool = Boolean

  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException

  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0
  override def boolean(b: Boolean): Value =
    if (b)
      Value.Int32(1)
    else
      Value.Int32(0)

  override type Addr = Int
  override type Bytes = Seq[Byte]
  override type Size = Int
  override type ExcV = WasmException[Value]
  override type FuncIx = Int
  override type FunV = FunctionInstance

  given ConcreteSpecialWasmOperations(using f: Failure): SpecialWasmOperations[Value, Addr, Size, FuncIx, NoJoin] with
    override def valueToAddr(v: Value): Int = v.asInt32
    override def valueToFuncIx(v: Value): Int = v.asInt32
    override def valToSize(v: Value): Int = v.asInt32
    override def sizeToVal(sz: Int): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionC[A] =
      val i = ix.asInt32
      if (i >= 0 && i < vec.size)
        JOptionC.Some(vec(i))
      else
        JOptionC.none


    val runtime: Map[HostFunction, List[Value] => List[Value]] = Map(
      HostFunction.proc_exit -> { args =>
        val exitCode = args.head
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      },
      HostFunction.fd_close -> { args => f.fail(FileError, s"Mock implementation of fd_close") },
      HostFunction.fd_read -> { args => f.fail(FileError, s"Mock implementation of fd_read") },
      HostFunction.fd_seek -> { args => f.fail(FileError, s"Mock implementation of fd_seek") },
      HostFunction.fd_write -> { args => f.fail(FileError, s"Mock implementation of fd_write") },
      HostFunction.fd_fdstat_get -> { args => f.fail(FileError, s"Mock implementation of fd_fdstat_get") },
      // TODO:
      //  Implement hostfunctions with help of WASI libc headers:
      //  https://github.com/WebAssembly/wasi-libc/blob/main/libc-bottom-half/headers/public/wasi/api.h
      HostFunction.args_sizes_get -> { args => f.fail(MockError, s"Mock implementation of args_sizes_get") },
      HostFunction.args_get -> { args => f.fail(MockError, s"Mock implementation of") },
      HostFunction.environ_sizes_get -> { args => f.fail(MockError, s"Mock implementation of") },
      HostFunction.environ_get -> { args => f.fail(MockError, s"Mock implementation of") },
      HostFunction.fd_prestat_get -> { args => f.fail(FileError, s"Mock implementation of") },
      HostFunction.random_get -> { args => f.fail(MockError, s"Mock implementation of") },
      HostFunction.path_open -> { args => f.fail(MockError, s"Mock implementation of") },
      HostFunction.fd_prestat_dir_name -> { args => f.fail(FileError, s"Mock implementation of") }
    )

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] =
      runtime(hostFunc)(args)

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value]) extends GenericInstance:

    override def jvUnit: NoJoin[Unit] = implicitly
    override def jvV: NoJoin[Value] = implicitly
    override def jvFunV: NoJoin[FunV] = implicitly

    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val memory: ConcreteMemory[MemoryAddr] = new ConcreteMemory[MemoryAddr]
    val globals: ConcreteSymbolTable[Unit, GlobalAddr, Value] = new ConcreteSymbolTable[Unit, GlobalAddr, Value]
    val funTable: ConcreteSymbolTable[TableAddr, FuncIx, FunV] = new ConcreteSymbolTable[TableAddr, FuncIx, FunV]
    val callFrame: ConcreteCallFrame[FrameData, Int, Value] = new ConcreteCallFrame[FrameData, Int, Value](rootFrameData, rootFrameValues.view.zipWithIndex.map(_.swap))
    val except: ConcreteExcept[WasmException[Value]] = new ConcreteExcept[WasmException[Value]]
    val failure: CFailure = new CFailure
    private given Failure = failure
    
    val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, NoJoin] = implicitly
    
    val fixpoint = new fix.ConcreteFixpoint[FixIn, FixOut[Value]]
    override val fixpointSuper = fixpoint

