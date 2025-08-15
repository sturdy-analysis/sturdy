package sturdy.language.wasm.analyses

import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.ConcreteCallFrame
import sturdy.effect.callframe.JoinableDecidableCallFrame
import sturdy.effect.symboltable.{ConstantIntervalMappedSymbolTable, IntervalMappedSymbolTable, JoinableDecidableSymbolTable, SizedConstantTable, SizedSymbolTable, joinLimit}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.SizedConstantTable.CombineTable
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
import sturdy.values.simd.{*, given}
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
  type RefV = Powerset[RefValue]

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Bytes, Size, Index, FunV, RefV, WithJoin] with
    override def valToAddr(v: Value): Addr = v.asInt32
    override def valToIdx(v: Value): Index = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Num(NumValue.Int32(sz))
    override def valToRef(v: ConstantAnalysis.Value, funcs: Vector[FunctionInstance]): Powerset[ConstantAnalysis.RefValue] = v match
      case ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.FuncRef(f)) => Powerset(ConstantAnalysis.RefValue.FuncRef(f))
      case ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.ExternRef(f)) => Powerset(ConstantAnalysis.RefValue.ExternRef(f))
      case ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.FuncNull) => Powerset(Set(ConstantAnalysis.RefValue.FuncNull))
      case ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.ExternNull) => Powerset(Set(ConstantAnalysis.RefValue.ExternNull))
      case ConstantAnalysis.Value.TopValue =>
        // powerset of all function references
        val funcRefs = funcs.map {
          case f@FunctionInstance.Wasm(_, _, _, _) => ConstantAnalysis.RefValue.FuncRef(Topped.Actual(f))
          case f@FunctionInstance.Host(_, _, _) => ConstantAnalysis.RefValue.FuncRef(Topped.Actual(f))
        }
        Powerset(funcRefs.toSet + ConstantAnalysis.RefValue.FuncNull + ConstantAnalysis.RefValue.ExternNull)

    override def refToVal(r: Powerset[ConstantAnalysis.RefValue]): ConstantAnalysis.Value = r match
      case Powerset(refs) =>
        if (refs.size != 1)
          Value.TopValue
        else {
          val ref = refs.head
          ref match {
            case RefValue.FuncRef(f) => Value.Ref(RefValue.FuncRef(f))
            case RefValue.ExternRef(f) => Value.Ref(RefValue.ExternRef(f))
            case RefValue.FuncNull => Value.Ref(RefValue.FuncNull)
            case RefValue.ExternNull => Value.Ref(RefValue.ExternNull)
          }
        }

    override def liftBytes(b: Seq[Byte]): Seq[Topped[Byte]] = b.map(Topped.Actual(_))

    override def funcInstToFunV(f: FunctionInstance): Powerset[FunctionInstance] = Powerset(f)

    override def funVToFuncInst(fVal: Powerset[FunctionInstance]): FunctionInstance =
      fVal match {
        case Powerset(funcs) =>
          if (funcs.isEmpty)
            f.fail(UnboundFunctionIndex, s"Cannot convert $fVal to function instance")
          else {
            val func = funcs.head
            if (funcs.size > 1)
              f.fail(UnboundFunctionIndex, s"Cannot convert $fVal to function instance")
            else
              func
          }
      }

    override def refVToFunV(r: Powerset[RefValue]): Powerset[FunctionInstance] =
      r match {
      case Powerset(refs) =>
        val funcs = refs.collect {
          case RefValue.FuncRef(Topped.Actual(f)) => f
          case RefValue.ExternRef(_) => f.fail(UnboundFunctionIndex, s"Cannot convert extern reference to actual function: $refs")
          case RefValue.FuncNull | RefValue.ExternNull => FunctionInstance.Null()
        }
        if (funcs.isEmpty) {
          f.fail(UnboundFunctionIndex, s"Cannot convert $refs to function instance")
        }
        Powerset(funcs)
    }


    override def funVToRefV(f: Powerset[FunctionInstance]): Powerset[RefValue] = {
      f match {
        case Powerset(funcs) =>
          if (funcs.isEmpty)
            Powerset(Set(RefValue.FuncNull))
          else {
            Powerset(funcs.map {
              case f@FunctionInstance.Wasm(_, _, _, _) => RefValue.FuncRef(Topped.Actual(f))
              case f@FunctionInstance.Host(_, _, _) => RefValue.FuncRef(Topped.Actual(f))
              case _ => RefValue.FuncNull
            })
          }
      }
    }

    override def makeNullRefV(t: ReferenceType): Powerset[ConstantAnalysis.RefValue] = {
      t match {
        case FuncRef => Powerset(Set(ConstantAnalysis.RefValue.FuncNull))
        case ExternRef => Powerset(Set(ConstantAnalysis.RefValue.ExternNull))
      }
    }

    override def isNullRef(r: Value): ConstantAnalysis.Value = {
      r match {
        case ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.FuncNull) => Value.Num(ConstantAnalysis.NumValue.Int32(Topped.Actual(1)))
        case ConstantAnalysis.Value.Ref(ConstantAnalysis.RefValue.ExternNull) => Value.Num(ConstantAnalysis.NumValue.Int32(Topped.Actual(1)))
        case ConstantAnalysis.Value.TopValue => Value.Num(ConstantAnalysis.NumValue.Int32(Topped.Top))
        case _ => Value.Num(ConstantAnalysis.NumValue.Int32(Topped.Actual(0)))
      }
    }

    override def addOffsetToAddr(offset: Int, addr: Topped[Int]): Topped[Int] = addr.map(_ + offset)

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
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.FuncRef(f)) =>
        f match
          case FunctionInstance.Wasm(_, _, _, _) => Value.Ref(RefValue.FuncRef(Topped.Actual(f)))
          case _ => Value.Ref(RefValue.FuncNull)
      case ConcreteInterpreter.Value.Ref(ConcreteInterpreter.RefValue.ExternRef(f)) => Value.Ref(RefValue.ExternRef(Topped.Actual(f)))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends GenericInstance:
    private given Instance = this

    var dummy: List[Value] = List()

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvBytes: WithJoin[Bytes] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
    override def jvRefV: WithJoin[RefV] = implicitly
//    override def widenState: Widen[State] = implicitly

    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: ConstantAddressMemory[MemoryAddr, Topped[Byte]] = new ConstantAddressMemory(Topped.Actual(0))
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val tables: ConstantIntervalMappedSymbolTable[Value, TableAddr, RefV] = new ConstantIntervalMappedSymbolTable[Value, TableAddr, RefV](extractor = (v: Value) => {
      val i32Val = v.asInt32
      i32Val match {
        case Topped.Actual(i) => NumericInterval(i, i)
        case Topped.Top => NumericInterval(Int.MinValue, Int.MaxValue)
      }
    })
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value, InstLoc] = new JoinableDecidableCallFrame(FrameData.empty, Iterable.empty)
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, Index, FunV, RefV, WithJoin] = implicitly

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
