package sturdy.language.wasm

import sturdy.data.{*, given}
import sturdy.effect.{Concrete, EffectStack, NoJoinsToObserve}
import sturdy.effect.bytememory.ConcreteMemory
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.ConcreteExcept
import sturdy.effect.failure.Failure
import sturdy.effect.failure.ConcreteFailure
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.symboltable.{ConcreteSizedTable, ConcreteSymbolTable, DecidableSymbolTable, SizedSymbolTable, SymbolTable}
import sturdy.fix
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.*
import sturdy.values.booleans.BooleanBranching
import sturdy.values.floating.FloatOps
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.*
import sturdy.values.exceptions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.simd.{*, given}
import sturdy.values.ordering.{*, given}
import swam.text.*
import swam.syntax.*
import swam.ReferenceType
import swam.ReferenceType.{ExternRef, FuncRef}

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
  override type V128 = Array[Byte]
  override type Bool = Boolean
  override type Reference = FunctionInstance | Int
  override type FunV = FunctionInstance
  override type RefV = RefValue
  override type Addr = Int
  override type Bytes = Seq[Byte]
  override type Size = Int
  override type ExcV = WasmException[Value]
  override type Index = Int

  override def topI32: I32 = throw new UnsupportedOperationException
  override def topI64: I64 = throw new UnsupportedOperationException
  override def topF32: F32 = throw new UnsupportedOperationException
  override def topF64: F64 = throw new UnsupportedOperationException
  override def topV128: V128 = throw new UnsupportedOperationException

  override def asBoolean(v: Value)(using Failure): Boolean = v.asInt32 != 0
  override def booleanToVal(b: Boolean): Value =
    if (b)
      Value.Num(NumValue.Int32(1))
    else
      Value.Num(NumValue.Int32(0))

  def eqVals(vs1: List[Value], vs2: List[Value]): Boolean =
    vs1.size == vs2.size && vs1.zip(vs2).forall {
      case (Value.Num(NumValue.Int32(i1)), Value.Num(NumValue.Int32(i2))) => i1 == i2
      case (Value.Num(NumValue.Int64(l1)), Value.Num(NumValue.Int64(l2))) => l1 == l2
      case (Value.Num(NumValue.Float32(f1)), Value.Num(NumValue.Float32(f2))) => f1.isNaN && f2.isNaN || f1 == f2
      case (Value.Num(NumValue.Float64(d1)), Value.Num(NumValue.Float64(d2))) => d1.isNaN && d2.isNaN || d1 == d2
      case (Value.Ref(RefValue.RefValue(r1)), Value.Ref(RefValue.RefValue(r2))) => r1 == r2
      case (Value.Vec(VecValue.Vec128(b1)), Value.Vec(VecValue.Vec128(b2))) =>
        val bb1 = ByteBuffer.wrap(b1)
        val bb2 = ByteBuffer.wrap(b2)

        val eqF32 = (0 until 16 by 4).forall { i =>
          val x = java.lang.Float.intBitsToFloat(bb1.getInt(i))
          val y = java.lang.Float.intBitsToFloat(bb2.getInt(i))
          if (x.isNaN && y.isNaN) true else bb1.getInt(i) == bb2.getInt(i)
        }

        val eqF64 = (0 until 16 by 8).forall { i =>
          val x = java.lang.Double.longBitsToDouble(bb1.getLong(i))
          val y = java.lang.Double.longBitsToDouble(bb2.getLong(i))
          if (x.isNaN && y.isNaN) true else bb1.getLong(i) == bb2.getLong(i)
        }

        eqF32 || eqF64
      case _ => false
    }

  def constExprToVals(e: unresolved.Expr): List[Value] =
    e.map(constExprToVal).toList

  def constExprToVal(inst: unresolved.Inst): Value =
    inst match
      case unresolved.i32.Const(i) => Value.Num(NumValue.Int32(i))
      case unresolved.i64.Const(l) => Value.Num(NumValue.Int64(l))
      case unresolved.f32.Const(f) => Value.Num(NumValue.Float32(f))
      case unresolved.f64.Const(d) => Value.Num(NumValue.Float64(d))
      case unresolved.v128.Const(v, _) => Value.Vec(ConcreteInterpreter.VecValue.Vec128(v))
      case unresolved.RefNull(t) =>
        t match
          case ReferenceType.FuncRef => makeRef(FunctionInstance.Null)
          case ReferenceType.ExternRef => makeRef(0)
      case unresolved.RefFunc(x) => x match {
        case Left(r) => throw new IllegalArgumentException(s"Cannot resolve unresolved funcref $r")
        case _ => Value.Ref(RefValue.RefValue(0))
      }
      case unresolved.RefExtern(x) => x match {
        case Left(r) => Value.Ref(RefValue.RefValue(r))
        case _ => Value.Ref(RefValue.RefValue(0))
      }
      case _ => throw IllegalArgumentException(s"Expected constant instruction but got $inst")

  given ConcreteSpecialWasmOperations(using f: Failure): SpecialWasmOperations[Value, Addr, Bytes, Size, Index, FunV, RefV, NoJoin] with
    override def valToAddr(v: Value): Int = v.asInt32
    override def valToIdx(v: Value): Int = v.asInt32
    override def valToSize(v: Value): Int = v.asInt32
    override def sizeToVal(sz: Int): Value = Value.Num(NumValue.Int32(sz))

    override def valToRef(v: Value, funcs: Vector[FunctionInstance]): RefValue = v match {
      case Value.Ref(ref) => ref
      case _ => f.fail(TypeError, s"Expected a reference value, but got $v")
    }
    
    override def refToVal(r: RefV): Value = Value.Ref(r)
    
    override def liftBytes(b: Seq[Byte]): Seq[Byte] = b

    override def funcInstToRefV(f: FunctionInstance): RefV =
      RefValue.RefValue(f)

    override def refVToFunV(r: RefV): FunV =
      r match {
        case RefValue.RefValue(f : FunctionInstance) => f
        case RefValue.RefValue(_) => f.fail(UnboundFunctionIndex, s"Cannot convert extern reference to actual function: $r")
      }
    
    override def makeNullRefV(t: ReferenceType): RefValue =
      t match
        case FuncRef => RefValue.RefValue(FunctionInstance.Null)
        case ExternRef => RefValue.RefValue(0)
    
    override def isNullRef(r: Value): Value =
      r match {
        case Value.Ref(RefValue.RefValue(FunctionInstance.Null)) => Value.Num(NumValue.Int32(1))
        case Value.Ref(RefValue.RefValue(0)) => Value.Num(NumValue.Int32(1))
        case _ => Value.Num(NumValue.Int32(0))
      }

    override def addOffsetToAddr(offset: Int, addr: Addr): Int =
      val resultAddr = addr + offset
      if(Integer.compareUnsigned(resultAddr, offset) < 0)
        f.fail(MemoryAccessOutOfBounds, s"$addr + $offset")
      else
        resultAddr

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
    override def jvBytes: NoJoin[Bytes] = implicitly
    override def jvV: NoJoin[Value] = implicitly
    override def jvFunV: NoJoin[FunV] = implicitly
    override def jvRefV: NoJoin[RefV] = implicitly
    override def jvElem: NoJoin[Elem] = implicitly

    override val stack: ConcreteOperandStack[Value] = new ConcreteOperandStack[Value]
    override val memory: ConcreteMemory[MemoryAddr] = new ConcreteMemory[MemoryAddr]
    override val globals: ConcreteSymbolTable[Unit, GlobalAddr, Value] = new ConcreteSymbolTable[Unit, GlobalAddr, Value]
    override val elems: ConcreteSymbolTable[Unit, ElemAddr, Elem] = new ConcreteSymbolTable
    override val tables: ConcreteSizedTable[ConcreteInterpreter.Value, TableAddr, ConcreteInterpreter.RefValue] = new ConcreteSizedTable[Value, TableAddr, RefValue](_.asInt32.toInt)
    override val callFrame: ConcreteCallFrame[FrameData, Int, Value, InstLoc] =
      new ConcreteCallFrame[FrameData, Int, Value, InstLoc](
        rootFrameData,
        rootFrameValues.view.map(Some(_)).zipWithIndex.map(_.swap)
      )
    override val except: ConcreteExcept[WasmException[Value]] = new ConcreteExcept[WasmException[Value]]
    override val failure: ConcreteFailure = new ConcreteFailure
    private given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, RefV, NoJoin] = implicitly

    override val fixpoint = new fix.ContextInsensitiveFixpoint[FixIn, FixOut[Value]] {
      override protected def contextInsensitive = fix.log(controlEventLogger(Instance.this, NoJoinsToObserve, except), fix.identity)
    }
