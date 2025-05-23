package sturdy.language.wasm

import sturdy.data.{*, given}
import sturdy.effect.{Concrete, EffectStack, NoJoinsToObserve}
import sturdy.effect.bytememory.ConcreteMemory
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.except.ConcreteExcept
import sturdy.effect.failure.Failure
import sturdy.effect.failure.ConcreteFailure
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.symboltable.{ConcreteSymbolTable, DecidableSymbolTable, SizedConcreteSymbolTable, SizedSymbolTable, TableOps, WrappedSymbolicTableOps}
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
  override type ExternReference = FunctionInstance

  override def topI32: Int = throw new UnsupportedOperationException
  override def topI64: Long = throw new UnsupportedOperationException
  override def topF32: Float = throw new UnsupportedOperationException
  override def topF64: Double = throw new UnsupportedOperationException
  override def topFuncRef: FuncReference = throw new UnsupportedOperationException
  override def topExternRef: FuncReference = throw new UnsupportedOperationException

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
  
  given ConcreteSpecialWasmOperations(using f: Failure): SpecialWasmOperations[Value, Addr, Size, Index, FunV, RefV, NoJoin] with
    override def valToAddr(v: Value): Int = v.asInt32
    override def valToIdx(v: Value): Int = v.asInt32
    override def valToSize(v: Value): Int = v.asInt32
    override def sizeToVal(sz: Int): Value = Value.Num(NumValue.Int32(sz))
    override def intToVal(i: Int): Value = Value.Num(NumValue.Int32(i))
    override def valToInt(v: Value): Int = v.asInt32

    override def valToRef(v: ConcreteInterpreter.Value): ConcreteInterpreter.RefValue = v match {
      case Value.Ref(ref) => ref
      case _ => f.fail(TypeError, s"Expected a reference value, but got $v")
    }
    
    override def refToVal(r: ConcreteInterpreter.RefValue): ConcreteInterpreter.Value = Value.Ref(r)
    
    override def refVToFunV(r: ConcreteInterpreter.RefValue): FunctionInstance = 
      r match {
        case RefValue.FuncRef(f) => f
        case RefValue.ExternRef(f) => f
        case RefValue.FuncNull | RefValue.ExternNull => f.fail(UnboundFunctionIndex, s"Expected a function reference, but got $r")
      }
    
    override def makeNullRefV(t: ReferenceType): ConcreteInterpreter.RefValue =
      t match {
        case FuncRef => ConcreteInterpreter.RefValue.FuncNull
        case ExternRef => ConcreteInterpreter.RefValue.ExternNull
      }

    override def funVToRefV(f: FunV, t: ReferenceType): ConcreteInterpreter.RefValue =
      t match {
        case FuncRef => f match {
          case FunctionInstance.Wasm(_, _, _, _) => ConcreteInterpreter.RefValue.FuncRef(f)
          case _ => ConcreteInterpreter.RefValue.FuncNull
        }
        case ExternRef => f match {
          case FunctionInstance.Wasm(_, _, _, _) => ConcreteInterpreter.RefValue.FuncRef(f)
          case _ => ConcreteInterpreter.RefValue.ExternNull
        }
      }
    
    override def isNull(r: ConcreteInterpreter.Value): ConcreteInterpreter.Value =
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
      
  given ConcreteTableOperations(using f: Failure): WrappedSymbolicTableOps[Value, TableAddr, Index, Size, RefV, NoJoin] with {
    val table = new SizedConcreteSymbolTable[TableAddr, RefV]

    override def initTable(table: TableAddr, elem: Vector[RefV], elemOffset: Value, tableOffset: Value, amount: Value): JOptionC[Unit] =
      // elem bounds check
      if (elemOffset.asInt32 < 0 || elemOffset.asInt32 + amount.asInt32 > elem.size) {
        return JOptionC.none
      }
      // table bounds check
      if (!inBounds(tableOffset, amount, table)) {
        return JOptionC.none
      }
      val newEntries = elem.slice(elemOffset.asInt32, elemOffset.asInt32 + amount.asInt32)
      for ((entry, index) <- newEntries.zipWithIndex) {
        this.table.set(table, tableOffset.asInt32 + index, entry)
      }
      JOptionC.some(())
    override def fillTable(table: TableAddr, entry: RefV, tableOffset: Value, amount: Value): JOptionC[Unit] =
      // table bounds check
      if(!inBounds(tableOffset, amount, table)) {
        return JOptionC.none
      }
      for (index <- tableOffset.asInt32 until tableOffset.asInt32 + amount.asInt32) {
        this.table.set(table, index, entry)
      }
      JOptionC.some(())
    override def copy(dstTable: TableAddr, srcTable: TableAddr, dstOffset: Value, srcOffset: Value, amount: Value): JOptionC[Unit] =
      // dst table bounds check
      if (!inBounds(dstOffset, amount, dstTable)) {
        return JOptionC.none
      }
      // src table bounds check
      if(!inBounds(srcOffset, amount, srcTable)) {
        return JOptionC.none
      }
      // copy entries to Vector
      var entries: Vector[RefV] = Vector.empty
      for (index <- 0 until amount.asInt32) {
        val entry = this.table.get(srcTable, srcOffset.asInt32 + index).getOrElse(return JOptionC.none)
        entries = entries :+ entry
      }
      for ((entry, index) <- entries.zipWithIndex) {
        this.table.set(dstTable, dstOffset.asInt32 + index, entry)
      }
      JOptionC.some(())

    private def inBounds(offset: Value, amount: Value, table: TableAddr): Boolean = offset.asInt32 >= 0 && offset.asInt32 + amount.asInt32 <= this.table.size(table)
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
    val tables: SizedConcreteSymbolTable[TableAddr, RefV] = new SizedConcreteSymbolTable[TableAddr, RefV]
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

