package sturdy.language.wasm

import sturdy.data.{*, given}
import sturdy.effect.bytememory.ConcreteMemory
import sturdy.effect.callframe.ConcreteCallFrame
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
  override type Ctx = Unit
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

  given ConcreteSpecialWasmOperations(using f: Failure): SpecialWasmOperations[Value, Addr, Size, FuncIx, FunV, NoJoin] with
    override def valueToAddr(v: Value): Int = v.asInt32
    override def valueToFuncIx(v: Value): Int = v.asInt32
    override def valToSize(v: Value): Int = v.asInt32
    override def sizeToVal(sz: Int): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): OptionC[A] =
      val i = ix.asInt32
      if (i >= 0 && i < vec.size)
        OptionC.Some(vec(i))
      else
        OptionC.none


    val runtime: Map[HostFunction, List[Value] => List[Value]] = Map(
      HostFunction.proc_exit -> { args =>
        val exitCode = args.head
        f.fail(ProcExit(exitCode), s"Exiting program with exit code $exitCode")
      }
    )

    override def invokeHostFunction(hostFunc: HostFunction, args: List[Value]): List[Value] =
      runtime(hostFunc)(args)

  class Effects(rootFrameData: FrameData, rootFrameValues: Iterable[Value])
    extends ConcreteOperandStack[Value]
      with ConcreteMemory[MemoryAddr]
      with Globals[Value]
      with ConcreteSymbolTable[TableAddr, FuncIx, FunV]
      with ConcreteCallFrame[FrameData, Int, Value]
      with ConcreteExcept[WasmException[Value]]
      with CFailure:

    override def initialCallFrameData = rootFrameData
    override def initialCallFrameVars = rootFrameValues.view.zipWithIndex.map(_.swap)
    override protected def makeGlobalsTable = new ConcreteSymbolTable[Unit, GlobalAddr, Value] {}

  class Instance(_effects: Effects)(using Failure)
    extends GenericInstance(_effects) with fix.Concrete[FixIn[Value], FixOut[Value]]:

    val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, NoJoin] = implicitly

  def apply(rootFrameData: FrameData, rootFrameValues: Iterable[Value]): Instance =
    val effects = new Effects(rootFrameData, rootFrameValues)
    given Failure = effects
    new Instance(effects)
