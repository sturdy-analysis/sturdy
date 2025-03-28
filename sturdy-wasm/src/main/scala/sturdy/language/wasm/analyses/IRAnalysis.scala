package sturdy.language.wasm.analyses

import sturdy.control.{ControlEvent, ControlObservable, FixpointControlEvent, RecordingControlObserver}
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.bytememory.{ConstantAddressMemory, TopMemory}
import sturdy.effect.bytememory.ConstantAddressMemory.CombineMem
import sturdy.effect.callframe.{ConcreteCallFrame, JoinableDecidableCallFrame}
import sturdy.effect.except.JoinedExcept
import sturdy.effect.failure.{*, given}
import sturdy.effect.operandstack.{JoinableDecidableOperandStack, given}
import sturdy.effect.symboltable.ConstantSymbolTable.CombineTable
import sturdy.effect.symboltable.{ConstantSymbolTable, JoinableDecidableSymbolTable, UpperBoundSymbolTable}
import sturdy.fix
import sturdy.fix.context.Sensitivity
import sturdy.ir.{*, given}
import sturdy.language.wasm.abstractions.*
import sturdy.language.wasm.abstractions.Control.Exc
import sturdy.language.wasm.abstractions.Fix.{*, given}
import sturdy.language.wasm.generic.WasmFailure.*
import sturdy.language.wasm.generic.{*, given}
import sturdy.language.wasm.{ConcreteInterpreter, Interpreter}
import sturdy.values.booleans.{*, given}
import sturdy.values.convert.{*, given}
import sturdy.values.exceptions.{*, given}
import sturdy.values.floating.{*, given}
import sturdy.values.functions.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.{*, given}
import swam.FuncType
import swam.syntax.*

import java.nio.{ByteBuffer, ByteOrder}
import scala.collection.IndexedSeqView

object IRAnalysis extends Interpreter, IRValues, ExceptionByTarget, ControlFlow, Control:
  type J[A] = WithJoin[A]
  type Addr = I32
  type Bytes = IR
  type Size = I32
  type FuncIx = I32
  type FunV = Powerset[FunctionInstance]

  def structuralEquals(v1: Value, v2: Value): Boolean =
    import Value.*  
    (v1, v2) match
      case (Int32(i1), Int32(i2)) => i1.structuralEquality(i2)
      case (Int64(l1), Int64(l2)) => l1.structuralEquality(l2)
      case (Float32(f1), Float32(f2)) => f1.structuralEquality(f2)
      case (Float64(d1), Float64(d2)) => d1.structuralEquality(d2)
      case _ => false

  given ConstantSpecialWasmOperations(using f: Failure, eff: EffectStack): SpecialWasmOperations[Value, Addr, Size, FuncIx, WithJoin] with
    override def valueToAddr(v: Value): Addr = v.asInt32
    override def valueToFuncIx(v: Value): FuncIx = v.asInt32
    override def valToSize(v: Value): Size = v.asInt32
    override def sizeToVal(sz: Size): Value = Value.Int32(sz)

    override def indexLookup[A](ix: Value, vec: Vector[A]): JOptionPowerset[A] =
      JOptionPowerset.NoneSome(Powerset(vec.toSet))

    override def invokeHostFunction(hostFunc: HostFunction, args: List[IRAnalysis.Value]): List[IRAnalysis.Value] = hostFunc.name match
      case "proc_exit" =>
        val exitCode = args.head
        f.fail(ProcExit, s"Exiting program with exit code $exitCode")
      case _ =>
        val result = hostFunc.funcType.t.map(typedTop).toList
        eff.joinWithFailure(result)(f.fail(FileError, s"in ${hostFunc.name}"))

  given valuesAbstractly: Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.Int32(i) => Value.Int32(IR.Const(i))
      case ConcreteInterpreter.Value.Int64(l) => Value.Int64(IR.Const(l))
      case ConcreteInterpreter.Value.Float32(f) => Value.Float32(IR.Const(f))
      case ConcreteInterpreter.Value.Float64(d) => Value.Float64(IR.Const(d))

  class Instance(rootFrameData: FrameData, rootFrameValues: Iterable[Value], config: WasmConfig) extends
      GenericInstance, ControlObservable[Control.Atom, Control.Section, Control.Exc, Control.Fx]
//      , WasmFixpoint[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, J](conf)
      :
    private given Instance = this

    private var currentFeedback: Option[(FixIn, IR.Feedback)] = None // May be unnecessary

    implicit val branchOps: IRBranching[Unit] = new IRBranching[Unit]

    given Join[IR] = (v1: IR, v2: IR) => branchOps.currentCond match
      case None => Changed(IR.Join(v1, v2))
      case Some(cond) =>
        println(s"$v1 or $v2")
        if (v1.structuralEquality(v2))
          Unchanged(v1)
        else if (branchOps.inElse)
          Changed(IR.Select(cond, v2, v1))
        else
          Changed(IR.Select(cond, v1, v2))

    given Widen[IR] = (v1: IR, v2: IR) => // Used only (?) for the return value of a recursive function
      if v1.structuralEquality(v2) then Unchanged(v1) else Changed(v2)

    override def jvUnit: WithJoin[Unit] = implicitly
    override def jvV: WithJoin[Value] = implicitly
    override def jvFunV: WithJoin[FunV] = implicitly
//    override def widenState: Widen[State] = implicitly


    val stack: JoinableDecidableOperandStack[Value] = new JoinableDecidableOperandStack
    val memory: TopMemory[MemoryAddr, IR, IR, IR] = new TopMemory[MemoryAddr, Addr, Bytes, Size]
    val globals: JoinableDecidableSymbolTable[Unit, GlobalAddr, Value] = new JoinableDecidableSymbolTable
    val funTable = new UpperBoundSymbolTable[TableAddr, FuncIx, FunV](Powerset.empty)
    val callFrame: JoinableDecidableCallFrame[FrameData, Int, Value, InstLoc] = new JoinableDecidableCallFrame(rootFrameData, rootFrameValues.view.map(Some(_)).zipWithIndex.map(_.swap))
    val except: JoinedExcept[WasmException[Value], ExcV] = new JoinedExcept
    val failure: CollectedFailures[WasmFailure] = new CollectedFailures with ObservableFailure(this)
    private given Failure = failure

    override val wasmOps: WasmOps[Value, Addr, Bytes, Size, ExcV, FuncIx, FunV, WithJoin] = implicitly

    val observedConfig = config.withObservers(Seq(this.triggerControlEvent))
    override val fixpoint: fix.ContextualFixpoint[FixIn, FixOut[IRAnalysis.Value]] = new fix.ContextualFixpoint {
      override type Ctx = observedConfig.ctx.Ctx
      val (contextPreparation, sensitivity) = observedConfig.ctx.make[IRAnalysis.Value]
      import observedConfig.ctx.finiteCtx
      override protected def contextFree = phi =>
        fix.log(controlEventLogger(Instance.this, effectStack, except), contextPreparation(phi))
      override protected def context: Sensitivity[FixIn, Ctx] = sensitivity
      override protected def contextSensitive = observedConfig.fix.get
    }

    override def toString: String = s"constant $config"
