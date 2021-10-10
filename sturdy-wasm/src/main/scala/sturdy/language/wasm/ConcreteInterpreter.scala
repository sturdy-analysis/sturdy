package sturdy.language.wasm


import sturdy.data.{*, given}
import sturdy.effect.bytememory.ConcreteMemory
import sturdy.effect.callframe.{CCallFrameNumbered, CMutableCallFrameNumbered}
import sturdy.effect.except.ConcreteExcept
import sturdy.effect.failure.{CFailure, Failure}
import sturdy.effect.operandstack.ConcreteOperandStack
import sturdy.effect.symboltable.ConcreteSymbolTable
import sturdy.fix
import sturdy.language.wasm.Interpreter
import sturdy.language.wasm.generic.*
import sturdy.values.booleans.BooleanBranching
import sturdy.values.doubles.DoubleOps
import sturdy.values.floats.FloatOps
import swam.syntax.*
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.*
import sturdy.values.doubles.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.floats.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.longs.{*, given}
import sturdy.values.relational.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder

object ConcreteInterpreter extends Interpreter:
  override type MayJoin[A] = NoJoin[A]
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
  override type FunV = FunctionInstance[Value]
  override type Symbol = FuncIx | GlobalAddr

  enum Entry:
    case Function(fun: FunV)
    case Global(glob: GlobalInstance[Value])

  given ConcreteSpecialWasmOperations(using f: Failure): SpecialWasmOperations[Value, Addr, Size, FuncIx, FunV, Symbol, Entry, NoJoin] with
    override def valueToAddr(v: Value): Int = v.asInt32
    override def valueToFuncIx(v: Value): Int = v.asInt32
    override def valToSize(v: Value): Int = v.asInt32
    override def sizeToVal(sz: Int): Value = Value.Int32(sz)

    override def funcIxToSymbol(funcIx: FuncIx): Symbol = funcIx
    override def globIxToSymbol(globalIdx: GlobalAddr): Symbol = globalIdx

    override def funVToEntry(funV: FunctionInstance[Value]): Entry = Entry.Function(funV)
    override def globIToEntry(globI: GlobalInstance[Value]): Entry = Entry.Global(globI)
    override def entryToFuncV(entry: Entry): FunctionInstance[Value] = entry match
      case Entry.Function(funV) => funV
      case Entry.Global(_) => throw new IllegalArgumentException(s"Expected a function, but got $entry.")
    override def entryToGlobI(entry: Entry): GlobalInstance[Value] = entry match
      case Entry.Global(globI) => globI
      case Entry.Function(_) => throw new IllegalArgumentException(s"Expected a global, but got $entry.")

    override def indexLookup[A](ix: Value, vec: Vector[A]): OptionC[A] =
      val i = ix.asInt32
      if (i >= 0 && i < vec.size)
        OptionC.Some(vec(i))
      else
        OptionC.none


    val runtime: Map[HostFunction, List[Value] => List[Value]] = Map(
      HostFunction.Exit() -> { args =>
        val exitCode = args.head
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      }
    )

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] =
      runtime(hostFunc)(args)

  class Effects(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value])
    extends ConcreteOperandStack[Value]
      with ConcreteMemory[MemoryAddr]
      with ConcreteSymbolTable[TableAddr, Symbol, Entry]
      with CMutableCallFrameNumbered[FrameData[Value], Value] with CCallFrameNumbered(rootFrameData, rootFrameValues)
      with ConcreteExcept[WasmException[Value]]
      with CFailure

  class Instance(_effects: Effects)(using Failure)
    extends GenericInstance(_effects):

    val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, Symbol, Entry, NoJoin] = implicitly

    val phi: fix.Combinator[FixIn[Value], FixOut[Value]] = fix.identity

  def apply(rootFrameData: FrameData[Value], rootFrameValues: Iterable[Value]): Instance =
    val effects = new Effects(rootFrameData, rootFrameValues)
    given Failure = effects
    new Instance(effects)
