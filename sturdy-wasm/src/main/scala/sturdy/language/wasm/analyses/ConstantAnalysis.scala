package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.{ConstantSymbolTable, JoinableDecidableSymbolTable}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.{*, given}
import sturdy.values.floating.FloatOps
import swam.syntax.*
import swam.{FuncType, ReferenceType}
import swam.ReferenceType.{ExternRef, FuncRef}
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.{*, given}

import java.nio.ByteBuffer
import java.nio.ByteOrder
import scala.collection.IndexedSeqView
import WasmFailure.*
import sturdy.control.{ControlEvent, ControlObservable, FixpointControlEvent, RecordingControlObserver}
import sturdy.language.wasm.abstractions.Control.Exc

object ConstantAnalysis extends Interpreter, ConstantValues, ExceptionByTarget, ControlFlow, Control:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = Seq[Topped[Byte]]
  type Size = I32
  type Index = I32
  type FunV = Powerset[FunctionInstance]

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Size, Index, FunV, WithJoin] with
    override def valToAddr(v: Value): Addr = v.asInt32
    override def valToIdx(v: Value): Index = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Num(NumValue.Int32(sz))
    override def valToInt(v: Value): Int = {
      v match {
        case ConstantAnalysis.Value.Num(ConstantAnalysis.NumValue.Int32(i)) => i match {
          case Topped.Actual(n) => n
          case Topped.Top => 0
        }
        case _ => 0
      }
    }
    override def intToVal(i: Int): Value = Value.Num(NumValue.Int32(sturdy.language.wasm.analyses.ConstantAnalysis.topI32))
    override def numToRef(v: Value): Value = {
      v match {
        case Value.Num(NumValue.Int32(Topped.Actual(-1))) => makeNullRef(FuncRef)
        case Value.Num(NumValue.Int32(r)) => Value.Ref(ConstantAnalysis.RefValue.FuncRef(r))
        case _ => makeNullRef(FuncRef)
      }
    }
    override def funcRefToInt(r: Value): Int =
      r match {
        case Value.Ref(ConstantAnalysis.RefValue.FuncRef(Topped.Actual(i))) => i
        case _ => -1
      }

    override def makeRef(f: FunctionInstance): ConstantAnalysis.Value =
      f match {
        case FunctionInstance.Wasm(_, funcIx, _, _) => Value.Ref(ConstantAnalysis.RefValue.FuncRef(Topped.Actual(funcIx)))
        case _ => Value.Ref(ConstantAnalysis.RefValue.FuncNull)
      }
    override def funcInstToFunV(f: FunctionInstance): Powerset[FunctionInstance] = Powerset(f)
    override def makeRef(f: Powerset[FunctionInstance]): ConstantAnalysis.Value = {
      Value.Ref(ConstantAnalysis.RefValue.FuncRef(Topped.Actual(0)))
    }
    override def makeNullRef(t: ReferenceType): ConstantAnalysis.Value = {
      t match {
        case FuncRef => ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.FuncNull)
        case ExternRef => ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.ExternNull)
      }
    }

    override def isNull(r: Value): ConstantAnalysis.Value = {
      r match {
        case ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.FuncNull) => Value.Num(ConstantAnalysis.NumValue.Int32(Topped.Actual(1)))
        case ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.ExternNull) => Value.Num(ConstantAnalysis.NumValue.Int32(Topped.Actual(1)))
        case _ => Value.Num(ConstantAnalysis.NumValue.Int32(Topped.Actual(0)))
      }
    }
    override def makeExternRef(f: Int): ConstantAnalysis.Value =
      f match {
        case -1 => makeNullRef(ExternRef)
        case _ => Value.Ref(ConstantAnalysis.RefValue.ExternRef(Topped.Actual(f)))
      }
    override def instToVal(i: Inst): ConstantAnalysis.Value =
      i match {
        case RefFunc(x) => Value.Ref(ConstantAnalysis.RefValue.FuncRef(Topped.Actual(x)))
        case _ => Value.Ref(ConstantAnalysis.RefValue.FuncNull)
      }
    override def validateTableElem(tabSz: Int, e: Int): Boolean = {
      if (e < 0 | e >= tabSz) {
        false
      } else true
    }
    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      ix.asInt32 match
        case Topped.Actual(i) =>
          if (i >= 0 && i < vec.size)
            JOptionPowerset.Some(Powerset(vec(i)))
          else
            JOptionPowerset.None()
        case Topped.Top =>
          if (vec.isEmpty)
            JOptionPowerset.None()
          else
            JOptionPowerset.NoneSome(Powerset(vec.toSet))

    override def invokeHostFunction(hostFunc: HostFunction, args: List[ConstantAnalysis.Value]): List[ConstantAnalysis.Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        eff.joinWithFailure(result)(f.fail(FileError, s"in ${hostFunc.name}"))

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int32(i)) => Value.Num(NumValue.Int32(Topped.Actual(i)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Int64(l)) => Value.Num(NumValue.Int64(Topped.Actual(l)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float32(f)) => Value.Num(NumValue.Float32(Topped.Actual(f)))
      case ConcreteInterpreter.Value.Num(ConcreteInterpreter.NumValue.Float64(d)) => Value.Num(NumValue.Float64(Topped.Actual(d)))
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.FuncNull) => Value.Ref(RefValue.FuncNull)
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.ExternNull) => Value.Ref(RefValue.ExternNull)
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.FuncRef(f)) => Value.Ref(RefValue.FuncRef(Topped.Actual(f)))
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.ExternRef(f)) => Value.Ref(RefValue.ExternRef(Topped.Actual(f)))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
      GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]
//      , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this
    
    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
//    override def widenState: Widen[State] = implicitly

    
    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: ConstantAddressMemory[MemoryAddr, Topped[Byte]] = new ConstantAddressMemory(Topped.Actual(0))
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val tables: ConstantSymbolTable[TableAddr, Int, Value] = new ConstantSymbolTable
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value, InstLoc] = new JoinableDecidableCallFrame(FrameData.empty, Iterable.empty)
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, WithJoin] = implicitly

    val observedConfig = config.withObservers(Seq(this.triggerControlEvent))
    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[ConstantAnalysis.Value]] = new fix.ContextualFixpoint {
      override type Ctx = observedConfig.ctx.Ctx
      val (contextPreparation, sensitivity) = observedConfig.ctx.make[ConstantAnalysis.Value]
      import observedConfig.ctx.finiteCtx
      override protected def contextFree = phi =>
        fix.log(controlEventLogger(Instance.this, effectStack, except), contextPreparation(phi))
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = observedConfig.fix.get
    }

    override def toString: String = s"constant $config"
