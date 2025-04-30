package sturdy.language.wasm

import sturdy.data.{*, given}
import sturdy.effect.{EffectStack, NoJoinsToObserve}
import sturdy.effect.bytememory.ConcreteMemory
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.ConcreteExcept
import sturdy.effect.failure.Failure
import sturdy.effect.failure.ConcreteFailure
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.symboltable.DecidableSymbolTable
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
import sturdy.values.references.{*, given}
import swam.ReferenceType
import swam.ReferenceType.{ExternRef, FuncRef}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import WasmFailure.*
import sturdy.control.{ControlObservable, RecordingControlObserver}
import sturdy.effect.symboltable.ConcreteSymbolTable
import sturdy.fix.{Combinator, Contextual}
import sturdy.language.wasm.abstractions.Control

object ConcreteInterpreter extends Interpreter with Control:
  override type J[A] = NoJoin[A]
  override type I32 = Int
  override type I64 = Long
  override type F32 = Float
  override type F64 = Double
  override type Bool = Boolean
  override type FuncReference = Int
  override type ExternReference = Int

  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException
  override def topFuncRef: Int = throw new UnsupportedOperationException
  override def topExternRef: Int = throw new UnsupportedOperationException

  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0
  override def boolean(b: Boolean): Value =
    if (b)
      Value.Num(NumValue.Int32(1))
    else
      Value.Num(NumValue.Int32(0))

  override type Addr = Int
  override type Bytes = Seq[Byte]
  override type Size = Int
  override type ExcV = WasmException[Value]
  override type Index = Int
  override type FunV = FunctionInstance

  given ConcreteSpecialWasmOperations(using f: Failure): SpecialWasmOperations[Value, Addr, Size, Index, FunV, NoJoin] with
    override def valToAddr(v: Value): Int = v.asInt32
    override def valToIdx(v: Value): Int = v.asInt32
    override def valToSize(v: Value): Int = v.asInt32
    override def sizeToVal(sz: Int): Value = Value.Num(NumValue.Int32(sz))
    override def intToVal(i: Int): Value = Value.Num(NumValue.Int32(i))
    override def valToInt(v: Value): Int = v.asInt32

    override def numToRef(v: Value): Value =
      v match {
      case Value.Num(NumValue.Int32(-1)) => makeNullRef(FuncRef)
      case Value.Num(NumValue.Int32(r)) => Value.Ref(ConcreteInterpreter.RefValue.FuncRef(r))
      case _ => makeNullRef(FuncRef)
    }
    override def funcRefToInt(r: ConcreteInterpreter.Value): Int =
      r match {
      case Value.Ref(ConcreteInterpreter.RefValue.FuncRef(i)) => i
      case _ => -1
      }

    override def makeNullRef(t: ReferenceType): ConcreteInterpreter.Value =
      t match {
        case FuncRef => Value.Ref(ConcreteInterpreter.RefValue.FuncNull)
        case ExternRef => Value.Ref(ConcreteInterpreter.RefValue.ExternNull)
      }

    override def makeRef(f: FunctionInstance): ConcreteInterpreter.Value =
      f match {
        case FunctionInstance.Wasm(_, funcIx, _, _) => Value.Ref(ConcreteInterpreter.RefValue.FuncRef(funcIx))
        case _ => Value.Ref(ConcreteInterpreter.RefValue.FuncNull)
      }

    override def makeExternRef(f: Int): Value =
      f match {
        case -1 => makeNullRef(ExternRef)
        case _ => Value.Ref(ConcreteInterpreter.RefValue.ExternRef(f))
      }

    override def isNull(r: ConcreteInterpreter.Value): ConcreteInterpreter.Value =
      r match {
        case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.FuncNull) => Value.Num(ConcreteInterpreter.NumValue.Int32(1))
        case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.ExternNull) => Value.Num(ConcreteInterpreter.NumValue.Int32(1))
        case _ => Value.Num(ConcreteInterpreter.NumValue.Int32(0))
      }

    override def funcInstToFunV(f: FunctionInstance): FunctionInstance = f

    override def instToVal(i: Inst): ConcreteInterpreter.Value =
      i match {
        case RefFunc(x) => Value.Ref(ConcreteInterpreter.RefValue.FuncRef(x))
        case _ => Value.Ref(ConcreteInterpreter.RefValue.FuncNull)
      }

    override def validateTableElem(tabSz: Int, e: Int): Boolean =
      if (e < 0 | e >= tabSz) {
        false
      } else true

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionC[A] =
      val i = ix.asInt32
      if (i >= 0 && i < vec.size)
        JOptionC.Some(vec(i))
      else
        JOptionC.none


    val runtime: Map[String, List[Value] => List[Value]] = Map(
      "proc_exit" -> { args =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      },
      "fd_close" -> { args => f.fail(FileError, s"Mock implementation of fd_close") },
      "fd_read" -> { args => f.fail(FileError, s"Mock implementation of fd_read") },
      "fd_seek" -> { args => f.fail(FileError, s"Mock implementation of fd_seek") },
      "fd_write" -> { args => f.fail(FileError, s"Mock implementation of fd_write") },
      "fd_fdstat_get" -> { args => f.fail(FileError, s"Mock implementation of fd_fdstat_get") },
      // TODO: Implement hostfunctions with help of WASI libc headers:
      //  https://github.com/WebAssembly/wasi-libc/blob/main/libc-bottom-half/headers/public/wasi/api.h
      "args_sizes_get" -> { args => f.fail(MockError, s"Mock implementation of args_sizes_get") },
      "args_get" -> { args => f.fail(MockError, s"Mock implementation of") },
      "environ_sizes_get" -> { args => f.fail(MockError, s"Mock implementation of") },
      "environ_get" -> { args => f.fail(MockError, s"Mock implementation of") },
      "fd_prestat_get" -> { args => f.fail(FileError, s"Mock implementation of") },
      "random_get" -> { args => f.fail(MockError, s"Mock implementation of") },
      "path_open" -> { args => f.fail(MockError, s"Mock implementation of") },
      "fd_prestat_dir_name" -> { args => f.fail(FileError, s"Mock implementation of") }
    )

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] =
      runtime(hostFunc.name)(args)

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value]) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:

    override def jvUnit: NoJoin[Unit] = implicitly(sturdy.data.MayJoin.NoJoin()) //not correct but works for now, to be fixed
    override def jvV: NoJoin[Value] = implicitly
    override def jvFunV: NoJoin[FunV] = implicitly

    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val memory: ConcreteMemory[MemoryAddr] = new ConcreteMemory[MemoryAddr]
    val globals: ConcreteSymbolTable[Unit, GlobalAddr, Value] = new ConcreteSymbolTable[Unit, GlobalAddr, Value]
    val tables: ConcreteSymbolTable[TableAddr, Index, Value] = new ConcreteSymbolTable[TableAddr, Index, Value]
    val callFrame: ConcreteCallFrame[FrameData, Int, Value, InstLoc] =
      new ConcreteCallFrame[FrameData, Int, Value, InstLoc](
        rootFrameData,
        rootFrameValues.view.map(Some(_)).zipWithIndex.map(_.swap)
      )
    val except: ConcreteExcept[WasmException[Value]] = new ConcreteExcept[WasmException[Value]]
    val failure: ConcreteFailure = new ConcreteFailure
    override var tableLimits: List[(Int, Option[Int])] = List()
    override var tableTypes: List[ReferenceType] = List()
    private given Failure = failure

    val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, NoJoin] = implicitly

    val fixpoint = new fix.ContextInsensitiveFixpoint[FixIn, FixOut[Value]] {
      override protected def contextInsensitive = fix.log(controlEventLogger(Instance.this, NoJoinsToObserve, except), fix.identity)
    }

