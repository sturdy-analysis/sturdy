package sturdy.language.wasm

import sturdy.data.{*, given}
import sturdy.effect.{Concrete, EffectStack, NoJoinsToObserve}
import sturdy.effect.bytememory.ConcreteMemory
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.ConcreteExcept
import sturdy.effect.failure.Failure
import sturdy.effect.failure.ConcreteFailure
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.symboltable.{ConcreteSizedTable, ConcreteSymbolTable, DecidableSymbolTable, SizedSymbolTable}
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
import sturdy.fix.{Combinator, Contextual}
import sturdy.language.wasm.abstractions.Control
import sturdy.values.Topped

object ConcreteInterpreter extends Interpreter with Control:
  override type J[A] = NoJoin[A]
  override type I32 = Int
  override type I64 = Long
  override type F32 = Float
  override type F64 = Double
  override type Bool = Boolean
  override type FuncReference = FunctionInstance
  override type ExternReference = Int

  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException
  override def topFuncRef: FuncReference = throw new UnsupportedOperationException
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
  override type RefV = RefValue
  
  given ConcreteSpecialWasmOperations(using f: Failure): SpecialWasmOperations[Value, Addr, Bytes, Size, Index, FunV, RefV, NoJoin] with
    override def valToAddr(v: Value): Int = v.asInt32
    override def valToIdx(v: Value): Int = v.asInt32
    override def valToSize(v: Value): Int = v.asInt32
    override def sizeToVal(sz: Int): Value = Value.Num(NumValue.Int32(sz))

    override def valToRef(v: ConcreteInterpreter.Value, funcs: Vector[FunctionInstance]): ConcreteInterpreter.RefValue = v match {
      case Value.Ref(ref) => ref
      case _ => f.fail(TypeError, s"Expected a reference value, but got $v")
    }
    
    override def refToVal(r: ConcreteInterpreter.RefValue): ConcreteInterpreter.Value = Value.Ref(r)
    
    override def liftBytes(b: Seq[Byte]): Seq[Byte] = b
    
    override def refVToFunV(r: ConcreteInterpreter.RefValue): FunctionInstance = 
      r match {
        case RefValue.FuncRef(f) => f
        case RefValue.ExternRef(_) => f.fail(UnboundFunctionIndex, s"Cannot convert extern reference to actual function: $r")
        case RefValue.FuncNull | RefValue.ExternNull => FunctionInstance.Null()
      }
    
    override def makeNullRefV(t: ReferenceType): ConcreteInterpreter.RefValue =
      t match {
        case FuncRef => ConcreteInterpreter.RefValue.FuncNull
        case ExternRef => ConcreteInterpreter.RefValue.ExternNull
      }

    override def funVToRefV(f: FunV): ConcreteInterpreter.RefValue =
        f match {
          case FunctionInstance.Wasm(_, _, _, _) => ConcreteInterpreter.RefValue.FuncRef(f)
          case FunctionInstance.Host(_, _, _) => ConcreteInterpreter.RefValue.FuncRef(f)
          case _ => ConcreteInterpreter.RefValue.FuncNull
        }

    
    override def isNullRef(r: ConcreteInterpreter.Value): ConcreteInterpreter.Value =
      r match {
        case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.FuncNull) => Value.Num(ConcreteInterpreter.NumValue.Int32(1))
        case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.ExternNull) => Value.Num(ConcreteInterpreter.NumValue.Int32(1))
        case _ => Value.Num(ConcreteInterpreter.NumValue.Int32(0))
      }

    override def funcInstToFunV(f: FunctionInstance): FunctionInstance = f
    override def funVToFuncInst(f: FunctionInstance): FunctionInstance = f

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionC[A] =
      val i = ix.asInt32
      if (i >= 0 && i < vec.size)
        JOptionC.Some(vec(i))
      else
        JOptionC.none

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] = hostFunc.name match {
      case "proc_exit" =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")

      case "fd_close" | "fd_read" | "fd_seek" | "fd_write" | "fd_fdstat_get" | "fd_prestat_get" | "fd_prestat_dir_name" =>
        f.fail(FileError, s"Mock implementation of ${hostFunc.name}")

      case "args_sizes_get" | "args_get" | "environ_sizes_get" | "environ_get" | "random_get" | "path_open" =>
        f.fail(MockError, s"Mock implementation of ${hostFunc.name}")

      case "__VERIFIER_nondet_bool" =>
        List(Value.Num(NumValue.Int32(scala.util.Random.nextInt(2))))

      case "__VERIFIER_nondet_char" | "__VERIFIER_nondet_uchar" =>
        List(Value.Num(NumValue.Int32(scala.util.Random.nextInt(256))))

      case "__VERIFIER_nondet_short" | "__VERIFIER_nondet_ushort" =>
        List(Value.Num(NumValue.Int32(if (scala.util.Random.nextFloat() < 0.1) 0 else scala.util.Random.nextInt(16))))

      case "__VERIFIER_nondet_int" | "__VERIFIER_nondet_long" | "__VERIFIER_nondet_uint" | "__VERIFIER_nondet_ulong" =>
        List(Value.Num(NumValue.Int32(if (scala.util.Random.nextFloat() < 0.1) 0 else scala.util.Random.nextInt(16))))

      case "__VERIFIER_nondet_longlong" | "__VERIFIER_nondet_ulonglong" =>
        List(Value.Num(NumValue.Int64(if (scala.util.Random.nextFloat() < 0.1) 0L else scala.util.Random.nextLong())))

      case "__VERIFIER_nondet_float" =>
        List(Value.Num(NumValue.Float32(if (scala.util.Random.nextFloat() < 0.1) 0.0f else scala.util.Random.nextFloat())))

      case "__VERIFIER_nondet_double" =>
        List(Value.Num(NumValue.Float64(if (scala.util.Random.nextFloat() < 0.1) 0.0 else scala.util.Random.nextDouble())))

      case "__blackhole_int" | "__blackhole_int_p" =>
        args

      case other =>
        f.fail(MockError, s"Unimplemented host function: $other")
    }

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value]) extends
    GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]:

    override def jvUnit: NoJoin[Unit] = implicitly 
    override def jvV: NoJoin[Value] = implicitly
    override def jvFunV: NoJoin[FunV] = implicitly
    override def jvRefV: NoJoin[RefV] = implicitly

    val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    val memory: ConcreteMemory[MemoryAddr] = new ConcreteMemory[MemoryAddr]
    val globals: ConcreteSymbolTable[Unit, GlobalAddr, Value] = new ConcreteSymbolTable[Unit, GlobalAddr, Value]
    val tables: ConcreteSizedTable[ConcreteInterpreter.Value, TableAddr, ConcreteInterpreter.RefValue] = new ConcreteSizedTable[Value, TableAddr, RefValue](_.asInt32.toInt)
    val callFrame: ConcreteCallFrame[FrameData, Int, Value, InstLoc] =
      new ConcreteCallFrame[FrameData, Int, Value, InstLoc](
        rootFrameData,
        rootFrameValues.view.map(Some(_)).zipWithIndex.map(_.swap)
      )
    val except: ConcreteExcept[WasmException[Value]] = new ConcreteExcept[WasmException[Value]]
    val failure: ConcreteFailure = new ConcreteFailure
    private given Failure = failure

    val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, RefV, NoJoin] = implicitly

    val fixpoint = new fix.ContextInsensitiveFixpoint[FixIn, FixOut[Value]] {
      override protected def contextInsensitive = fix.log(controlEventLogger(Instance.this, NoJoinsToObserve, except), fix.identity)
    }

