package sturdy.language.wasm.generic

import sturdy.data.MayJoin
import sturdy.data.noJoin
import sturdy.effect.AnalysisState
import sturdy.effect.ComputationJoiner
import sturdy.effect.EffectStack
import sturdy.effect.Effectful
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.{Soundness, fix, IsSound}
import sturdy.effect.operandstack.OperandStack
import sturdy.effect.bytememory.Memory
import sturdy.effect.failure.AFailureCollect
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.symboltable.DecidableSymbolTable
import sturdy.effect.symboltable.SymbolTable
import sturdy.effect.symboltable.JoinedSymbolTable
import sturdy.values.Combine
import sturdy.values.Finite
import sturdy.values.MaybeChanged
import sturdy.values.Widening
import sturdy.values.booleans.BooleanBranching
import sturdy.values.exceptions.Exceptional
import sturdy.values.convert.*
import sturdy.values.floating.*
import sturdy.values.functions.FunctionOps
import sturdy.values.integer.*
import sturdy.values.relational.*
import swam.{ValType, GlobalType, LabelIdx, MemType, FuncType, TableType, GlobalIdx, FuncIdx, OpCode, BlockType, Limits}
import swam.syntax.*

import scala.collection.immutable.VectorBuilder
import scala.collection.mutable

case class FrameData(returnArity: Int, module: ModuleInstance):
  override def toString: String =
    if (module == null)
      s"null:$returnArity"
    else
      s"$module:$returnArity"
given FiniteFrameData: Finite[FrameData] with {}

given frameDataIsSound: Soundness[FrameData, FrameData] with
  override def isSound(c: FrameData, a: FrameData): IsSound =
    if (c.returnArity != a.returnArity)
      return IsSound.NotSound(s"Return arities do not match: $c $a.")
    if (c.module == null && a.module == null)
      return IsSound.Sound
    if (c.module != null && a.module != null)
      return IsSound.NotSound(s"Concrete module ${c.module} not approximated by ${a.module}")
    summon[Soundness[ModuleInstance, ModuleInstance]].isSound(c.module, a.module)

object FrameData:
  val empty: FrameData = FrameData(0, null)

enum WasmException[V]:
  case Jump(labelIndex: LabelIdx, operands: List[V])
  case Return(operands: List[V])

type Imports = mutable.Map[String, ModuleInstance]

case class FuncId(mod: ModuleInstance, funcIx: Int):
  override def toString: String = s"$mod.$funcIx"

enum InstLoc:
  case InFunction(func: FuncId, pc: Int)
  case InInit(mod: ModuleInstance, pc: Int)
  case InvokeExported(mod: ModuleInstance, funName: String)

  override def toString: String = this match
    case InFunction(func, pc) => s"$func:$pc"
    case InInit(mod, pc) => s"$mod.INIT:$pc"
    case InvokeExported(mod, funName) => s"$mod.$funName"

  def +(i: Int): InstLoc = this match
    case InFunction(func, pc) => InFunction(func, pc + i)
    case InInit(mod, pc) => InInit(mod, pc + i)
    case InvokeExported(mod, funName) => throw new IllegalStateException

  def -(that: InstLoc): Int = (this, that) match
    case (InFunction(func1, pc1), InFunction(func2, pc2)) if func1 == func2 => pc2 - pc1
    case (InInit(mod1, pc1), InInit(mod2, pc2)) if mod1 == mod2 => pc2 - pc1
    case _ => throw new MatchError((this, that))

enum FixIn:
  case Eval(inst: Inst, loc: InstLoc)
  case EnterWasmFunction(id: FuncId, func: Func, ft: FuncType)

  override def toString: String = this match
    case Eval(i, loc) => i match
      case Block(_, _) => s"Block @$loc"
      case Loop(_, _) => s"Loop @$loc"
      case If(_, _, _) => s"If @$loc"
      case _ => s"$i @$loc"
    case EnterWasmFunction(id, _, _) => s"Enter $id"

enum FixOut[V]:
  case Eval()
  case ExitWasmFunction(vals: List[V])

given finiteFixIn: Finite[FixIn] with {}


trait GenericInterpreter[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, J[_] <: MayJoin[_]]
  extends fix.Fixpoint[FixIn, FixOut[V]]:

  implicit def jvUnit: J[Unit]
  implicit def jvV: J[V]
  implicit def jvFunV: J[FunV]

  // value components
  val wasmOps: WasmOps[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, J]
  import wasmOps.*
  import specialOps.*
  import exceptOps.*

  // effect components
  val stack: DecidableOperandStack[V]
  val memory: Memory[MemoryAddr, Addr, Bytes, Size, J]
  val globals: DecidableSymbolTable[Unit, GlobalAddr, V]
  val funTable: SymbolTable[TableAddr, FuncIx, FunV, J]
  val callFrame: DecidableMutableCallFrame[FrameData, Int, V]
  val except: Except[WasmException[V], ExcV, J]
  val failure: Failure

  import except.*

  // effect stack
  val effectStack: EffectStack = new EffectStack(List(stack, memory, globals, funTable, callFrame, except, failure))
  given EffectStack = effectStack

  // analysis state
  enum InState:
    case EnterFunState(vars: callFrame.Locals, mem: memory.State, globs: globals.State)
    case InstructionState(ops: stack.OperandFrame, vars: callFrame.Locals, mem: memory.State, globs: globals.State)
  enum OutState:
    case ExitFunState(ops: stack.OperandFrame, mem: memory.State, globs: globals.State)
    case InstructionState(ops: stack.OperandFrame, vars: callFrame.Locals, mem: memory.State, globs: globals.State)

  given CombineInState[W <: Widening]
    (using cOps: Combine[stack.OperandFrame, W], cVars: Combine[callFrame.Locals, W], cMem: Combine[memory.State, W], cGlobs: Combine[globals.State, W]):
    Combine[InState, W] with
    override def apply(v1: InState, v2: InState): MaybeChanged[InState] = (v1, v2) match
      case (s1: InState.EnterFunState, s2: InState.EnterFunState) =>
        val vars = cVars(s1.vars, s2.vars)
        val mem = cMem(s1.mem, s2.mem)
        val globs = cGlobs(s1.globs, s2.globs)
        MaybeChanged(InState.EnterFunState(vars.get, mem.get, globs.get), vars.hasChanged || mem.hasChanged || globs.hasChanged)
      case (s1: InState.InstructionState, s2: InState.InstructionState) =>
        val ops = cOps(s1.ops, s2.ops)
        val vars = cVars(s1.vars, s2.vars)
        val mem = cMem(s1.mem, s2.mem)
        val globs = cGlobs(s1.globs, s2.globs)
        MaybeChanged(InState.InstructionState(ops.get, vars.get, mem.get, globs.get), ops.hasChanged || vars.hasChanged || mem.hasChanged || globs.hasChanged)
      case _ => throw new IllegalArgumentException(s"Combine($v1, $v2)")
  
  given CombineOutState[W <: Widening]
    (using cOps: Combine[stack.OperandFrame, W], cVars: Combine[callFrame.Locals, W], cMem: Combine[memory.State, W], cGlobs: Combine[globals.State, W]):
    Combine[OutState, W] with
    override def apply(v1: OutState, v2: OutState): MaybeChanged[OutState] = (v1, v2) match
      case (s1: OutState.ExitFunState, s2: OutState.ExitFunState) =>
        val ops = cOps(s1.ops, s2.ops)
        val mem = cMem(s1.mem, s2.mem)
        val globs = cGlobs(s1.globs, s2.globs)
        MaybeChanged(OutState.ExitFunState(ops.get, mem.get, globs.get), ops.hasChanged || mem.hasChanged || globs.hasChanged)
      case (s1: OutState.InstructionState, s2: OutState.InstructionState) =>
        val ops = cOps(s1.ops, s2.ops)
        val vars = cVars(s1.vars, s2.vars)
        val mem = cMem(s1.mem, s2.mem)
        val globs = cGlobs(s1.globs, s2.globs)
        MaybeChanged(OutState.InstructionState(ops.get, vars.get, mem.get, globs.get), ops.hasChanged || vars.hasChanged || mem.hasChanged || globs.hasChanged)
      case _ => throw new IllegalArgumentException(s"Combine($v1, $v2)")
  
  type State = (stack.OperandFrame, memory.State, globals.State, callFrame.Locals)
  implicit def analysisState: AnalysisState[FixIn, InState, OutState, State] = new AnalysisState {
    override def getInState(dom: FixIn): InState = dom match
      case _: FixIn.EnterWasmFunction => InState.EnterFunState(callFrame.getLocals, memory.getState, globals.getState)
      case _: FixIn.Eval => InState.InstructionState(stack.getOperandFrame, callFrame.getLocals, memory.getState, globals.getState)
    override def setInState(in: InState): Unit = in match
      case InState.EnterFunState(vars, mem, globs) =>
        callFrame.setLocals(vars)
        memory.setState(mem)
        globals.setState(globs)
      case InState.InstructionState(ops, vars, mem, globs) =>
        stack.setOperandFrame(ops)
        callFrame.setLocals(vars)
        memory.setState(mem)
        globals.setState(globs)
    override def getOutState(dom: FixIn): OutState = dom match
      case _: FixIn.EnterWasmFunction => OutState.ExitFunState(stack.getOperandFrame, memory.getState, globals.getState)
      case _: FixIn.Eval => OutState.InstructionState(stack.getOperandFrame, callFrame.getLocals, memory.getState, globals.getState)
    override def setOutState(out: OutState): Unit = out match
      case OutState.ExitFunState(ops, mem, globs) =>
        stack.setOperandFrame(ops)
        memory.setState(mem)
        globals.setState(globs)
      case OutState.InstructionState(ops, vars, mem, globs) =>
        stack.setOperandFrame(ops)
        callFrame.setLocals(vars)
        memory.setState(mem)
        globals.setState(globs)
    override def getAllState: State = (stack.getOperandFrame, memory.getState, globals.getState, callFrame.getLocals)
    override def setAllState(in: State): Unit =
      stack.setOperandFrame(in._1)
      memory.setState(in._2)
      globals.setState(in._3)
      callFrame.setLocals(in._4)
  }

  private given Failure = failure
  lazy val num = new GenericInterpreterNumerics[V, J](stack, wasmOps)

  private val labelStack = new LabelStack
  private var memCount = 0
  private var tabCount = 0
  private var globCount = 0

  inline private def fail(k: FailureKind, what: String) = failure.fail(k, s"$what in $module")

  def module: ModuleInstance = callFrame.data.module

  def evalVarInst(inst: VarInst): Unit = inst match
    case LocalGet(ix) =>
      val v = callFrame.getLocal(ix).getOrElse(fail(UnboundLocal, ix.toString))
      stack.push(v)
    case LocalSet(ix) =>
      val v = stack.popOrAbort()
      callFrame.setLocal(ix, v).getOrElse(fail(UnboundLocal, ix.toString))
    case LocalTee(ix) =>
      val v = stack.peekOrAbort()
      callFrame.setLocal(ix, v).getOrElse(fail(UnboundLocal, ix.toString))
    case GlobalGet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val global = getGlobalValue(globalIdx)
      stack.push(global)
    case GlobalSet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val v = stack.popOrAbort()
      val _ = getGlobalValue(globalIdx)
      writeGlobalValue(globalIdx, v)

  def evalMemoryInst(inst: Inst): Unit = inst match
    case i: LoadInst => load(i)
    case i: LoadNInst => load(i)

    case i: StoreInst => store(i)
    case i: StoreNInst => store(i)

    case MemorySize =>
      val sz = memory.size(memoryIndex)
      stack.push(sizeToVal(sz))
    case MemoryGrow =>
      val delta = valToSize(stack.popOrAbort())
      val res = memory.grow(memoryIndex, delta).option
        (num.evalNumeric(i32.Const(0xFFFFFFFF))) // 0xFFFFFFFF ~= -1
        (sizeToVal)
      stack.push(res)
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def load(inst: LoadInst | LoadNInst): Unit =
    val addr = effectiveAddr(inst.offset)
    val memIdx = memoryIndex
    val length = getBytesToRead(inst)
    memory.read(memIdx,addr,length).option
      (fail(MemoryAccessOutOfBounds, s"Cannot read $length bytes at address $addr in current memory."))
      {(b: Bytes) =>
        val v = decode(b, SomeCC(inst, false))
        stack.push(v)}

  def store(inst: StoreInst | StoreNInst): Unit =
    val v = stack.popOrAbort()
    val bytes = encode(v, SomeCC(inst, false))

    // add offset to base address (which is already on the stack)
    stack.push(i32ops.integerLit(inst.offset))
    val addr = valueToAddr(num.evalNumeric(i32.Add))

    val memIdx = memoryIndex
    memory.write(memIdx, addr, bytes).getOrElse(
      fail(MemoryAccessOutOfBounds, s"Cannot write $bytes at address $addr in current memory.")
    )

  def getBytesToRead(inst: MemoryInst): Int = inst match
    case Load(tpe,_,_) => tpe.width / 8
    case LoadN(_,n,_,_) => n / 8
    case _ => throw new IllegalArgumentException(s"Expected load instruction, but got $inst")

  def memoryIndex: MemoryAddr =
    module.memoryAddrs(0)

  def tableIndex: TableAddr =
    module.tableAddrs(0)

  def globalTableIndex: TableAddr =
    TableAddr(0)

  def getGlobalValue(ga: GlobalAddr): V =
    globals.get((), ga).getOrElse(fail(UnboundGlobal, ga.toString))
  def writeGlobalValue(ga: GlobalAddr, v: V): Unit =
    globals.set((), ga, v)

  def eval_open(inst: Inst, loc: InstLoc)(using Fixed): Unit =
    val opcode = inst.opcode
    if (opcode >= OpCode.I32Const && opcode <= OpCode.I64Extend32S)
      stack.push(num.evalNumeric(inst))
    else if (opcode >= OpCode.I32Load && opcode <= OpCode.MemoryGrow)
      evalMemoryInst(inst)
    else if (opcode >= OpCode.Unreachable && opcode <= OpCode.CallIndirect)
      evalControlInst(inst, loc)
    else inst match
      case i: VarInst => evalVarInst(i)
      case op: Miscop =>
        val v = stack.popOrAbort()
        stack.push(num.evalMiscop(op, v))
      case Drop => stack.popOrAbort()
      case Select =>
        val isZero = num.evalNumeric(i32.Eqz)
        branchOpsUnit.boolBranch(isZero) {
          // v == 0: else branch
          val (_, v2) = stack.pop2OrAbort()
          stack.push(v2)
        } {
          stack.popOrAbort()
        }
      case _ => throw new IllegalArgumentException(s"Unexpected instruction $inst")

  def evalControlInst(inst: Inst, loc: InstLoc)(using Fixed): Unit = inst match
    case Nop => // nothing
    case Unreachable => fail(UnreachableInstruction, inst.toString)
    case b@Block(bt, insts) =>
      label(BlockId(b), labelArities(bt, isLoop = false), insts, None)
    case l@Loop(bt, insts) =>
      label(BlockId(l), labelArities(bt, isLoop = true), insts, Some((l, loc)))
    case ifInst@If(bt, thnInsts, elsInsts) =>
      val isZero = num.evalNumeric(i32.Eqz)
      val ars = labelArities(bt, isLoop = false)
      branchOpsUnit.boolBranch(isZero) {
        // v == 0: else branch
        label(BlockId(ifInst -> false), ars, elsInsts, None)
      } {
        label(BlockId(ifInst -> true), ars, thnInsts, None)
      }
    case Br(labelIndex) =>
      branch(labelIndex)
    case BrIf(labelIndex) =>
      val isZero = num.evalNumeric(i32.Eqz)
      branchOpsUnit.boolBranch(isZero) {
        // v == 0: else branch
        // do nothing
      } {
        branch(labelIndex)
      }
    case BrTable(labels, defaultLabel) =>
      val ix = stack.popOrAbort()
      indexLookup(ix, labels).orElseAndThen(defaultLabel)(branch)
    case Return =>
      val operands = stack.popNOrAbort(callFrame.data.returnArity)
      throws(WasmException.Return(operands))
    case Call(funcIx) =>
      val func = module.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
      invoke(func)
    case CallIndirect(typeIx) =>
      val ftExpected = module.functionTypes(typeIx)
      val funcIx = stack.popOrAbort()
      val func = funTable.get(tableIndex, valueToFuncIx(funcIx)).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
      invokeIndirect(func, ftExpected, funcIx)
    case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")

  def branch(labelIndex: LabelIdx): Unit =
    val returnArity = labelStack.lookupReturnArity(labelIndex)
    val operands = stack.popNOrAbort(returnArity)
    throws(WasmException.Jump(labelIndex, operands))

  /** Arities used by a label. Results equals jumpOperands if branchTarget is None. */
  case class LabelArities(params: Int, results: Int, jumpOperands: Int)

  private inline def assertFrameSize(size: Int): Unit =
    if (Debug.DEBUG_GENERIC_WASM_STACK && stack.frameSize != size)
      throw new AssertionError(s"Expected stack frame of size $size, but current stack frame has size ${stack.frameSize}")

  def label(block: BlockId, arities: LabelArities, insts: Iterable[Inst], branchTarget: Option[(Inst, InstLoc)])(using Fixed): Unit =
    stack.withNewFrame(arities.params) {
      tryCatch {
        labelStack.pushLabel(arities.jumpOperands)
        try {
          val modInst = module
          for ((inst, ix) <- insts.zipWithIndex) {
            val loc = modInst.blockInstLocs((block, ix))
            eval(inst, loc)
          }
          assertFrameSize(arities.results)
        } finally labelStack.popLabel()
      } { ex =>
        stack.clearCurrentOperandFrame()
        ex match {
          case WasmException.Jump(labelIndex, operands) =>
            if (labelIndex == 0) {
              stack.pushN(operands)
              assertFrameSize(arities.jumpOperands)
              for ((i,loc) <- branchTarget)
                eval(i, loc)
              assertFrameSize(arities.results)
            } else {
              assertFrameSize(0)
              throws(WasmException.Jump(labelIndex - 1, operands))
            }
          case _: WasmException.Return[V] =>
            assertFrameSize(0)
            throws(ex)
        }
      }
    }

  def invoke(fun: FunctionInstance)(using Fixed): Unit =
    fun match
      case FunctionInstance.Wasm(mod, ix, func, funcType) =>
        val args = stack.popNOrAbort(funcType.params.size)
        val frameData = FrameData(funcType.t.size, mod)
        val vars = args.view ++ func.locals.map(num.defaultValue)
        labelStack.withNew(stack.withNewFrame(0)(callFrame.withNew(frameData, vars.view.zipWithIndex.map(_.swap)) {
          enterFunction(FuncId(mod, ix), func, funcType)
        }))
      case FunctionInstance.Host(hostFunc) =>
        val args = stack.popNOrAbort(hostFunc.funcType.params.size)
        val res = invokeHostFunction(hostFunc, args)
        val expectedSize = hostFunc.funcType.t.size
        if (res.length != expectedSize) {
          throw new Error(s"Host function returned the wrong number of results: expected $expectedSize, but got ${res.length}.")
        }
        stack.pushN(res)

  def enterFunction_open(id: FuncId, func: Func, funcType: FuncType)(using Fixed): List[V] =
    val returnN = funcType.t.size
    tryCatch {
      label(BlockId(id), LabelArities(0, returnN, returnN), func.body, None)
    } { ex =>
      stack.clearCurrentOperandFrame()
      ex match {
        case WasmException.Return(operands) =>
          stack.pushN(operands)
        case WasmException.Jump(_, _) =>
          fail(InvalidModule, s"Tried to jump through a function boundary.")
      }
    }
    stack.peekNOrAbort(returnN)

  def invokeIndirect(funV: FunV, ftExpected: swam.FuncType, funcIx: V)(using Fixed): Unit =
    functionOps.invokeFun(funV, ftExpected) {
      case (func, _) =>
        val ftActual = func.funcType
        if (ftExpected != ftActual)
          fail(IndirectCallTypeMismatch, s"Expected function of type $ftExpected but $funcIx has type $ftActual")
        invoke(func)
    }


  inline def eval(inst: Inst, loc: InstLoc)(using rec: Fixed): FixOut[V] =
    rec(FixIn.Eval(inst, loc))
  inline def enterFunction(id: FuncId, func: Func, ft: FuncType)(using rec: Fixed): FixOut[V] =
    rec(FixIn.EnterWasmFunction(id, func, ft))

  private def fixed: Fixed = fixpoint {
    case FixIn.Eval(inst, loc) =>
      eval_open(inst, loc)
      FixOut.Eval()
    case FixIn.EnterWasmFunction(id, func, funcType) =>
      FixOut.ExitWasmFunction(enterFunction_open(id, func, funcType))
  }
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  def invokeExported(modInst: ModuleInstance, funcName: String, args: List[V]): List[V] = external {
    stack.withNewStack {
      modInst.exports.find(entry => entry._1 == funcName) match
        case Some((_, ExternalValue.Function(funcIx))) =>
          val fun = modInst.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
          val paramTys = fun.funcType.params
          if (paramTys.length != args.length)
            throw new Error(s"Wrong number of arguments in external invocation. Expected ${paramTys.length} but got ${args.length}.")
          // paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
          val rtLength = fun.funcType.t.length
          stack.pushN(args)
          callFrame.withNew(FrameData(0, modInst), Iterable.empty) {
            eval(Call(funcIx), InstLoc.InvokeExported(modInst, funcName))
          }
          stack.popNOrAbort(rtLength)
        case _ => throw new Error(s"Function with name $funcName was not found in module's exports.")
    }
  }


  private def labelArities(bt: BlockType, isLoop: Boolean): LabelArities = bt match
    case swam.BlockType.NoType => LabelArities(0, 0, 0)
    case _: swam.BlockType.ValueType =>
      if (isLoop)
        LabelArities(0, 1, 0)
      else
        LabelArities(0, 1, 1)
    case swam.BlockType.FunctionType(tpe) =>
      val ft = module.functionTypes(tpe)
      val params = ft.params.size
      val results = ft.t.size
      if (isLoop)
        LabelArities(params, results, params)
      else
        LabelArities(params, results, results)


//  // placeholder for the (not yet present in swam) memory.init instruction
//  def memoryInit(dataIdx: Int): Unit =
//    val dataInstance = module.data(dataIdx)
//    val cnt = stack.pop() // i32
//    val src = stack.pop() // i32
//    val dst = stack.pop() // i32
//    // check ranges TODO
//    //if (src + cnt > dataInstance.data.size)
//    // TODO WIP
//    ???

  def evalInstructionSequence(block: BlockId, insts: Vector[Inst], mod: ModuleInstance)(using Fixed): V =
    val frameData = FrameData(1,mod)
    labelStack.withNew(stack.withNewStack(callFrame.withNew(frameData, Iterable.empty){
      for ((inst, ix) <- insts.zipWithIndex) {
        val loc = mod.blockInstLocs((block, ix))
        eval(inst, loc)
      }
      stack.popOrAbort()
    }))

  /** add offset to base address (which is already on the stack) */
  def effectiveAddr(offset: Int): Addr =
    val v1 = i32ops.integerLit(offset)
    val v2 = stack.popOrAbort()
    val res = i32ops.add(v1,v2)
    val cmp = unsignedCompareOps.ltUnsigned(res,v1)
    val v = branchOpsV.boolBranch(cmp, fail(IntegerOverflow, s"$v1 + $v2"), res)
    valueToAddr(v)

  def resolveImports(module: Module, imports: Imports):
    (Vector[FunctionInstance], Vector[GlobalAddr], Vector[TableAddr], Vector[MemoryAddr]) =
    val funcs: VectorBuilder[FunctionInstance] = VectorBuilder()
    val globs: VectorBuilder[GlobalAddr] = VectorBuilder()
    val tabs: VectorBuilder[TableAddr] = VectorBuilder()
    val mems: VectorBuilder[MemoryAddr] = VectorBuilder()

    module.imports.foreach { imp =>
      // handle host functions
      if (imp.moduleName == "wasi_snapshot_preview1" || imp.moduleName == "wasi_unstable") {
        imp match
          case Import.Function(_,funcName, funcType) =>
            val hf = HostFunction.nameToHostFunction(funcName)
            if (hf.funcType != module.types(funcType))
              throw new Error(s"Importing host function $funcName with wrong type: expected ${hf.funcType}, but imported with ${module.types(funcType)}")
            funcs += FunctionInstance.Host(hf)
          case _ => throw new Error(s"Import from runtime: expected a function, but got $imp.")
      } else {
        // get the module to import from
        val from = imports.getOrElse(imp.moduleName, throw new Error(s"No module with name ${imp.moduleName} in imports."))
        // get the exported field
        val (_, exp) = from.exports.find((name, _) => name == imp.fieldName)
          .getOrElse(throw new Error(s"No export with name ${imp.fieldName} in module."))
        imp match
          case Import.Function(_, _, tpe) =>
            exp match
              case ExternalValue.Function(addr) =>
                val expectedType = module.types(tpe)
                val func = from.functions(addr)
                if (expectedType == func.funcType) {
                  funcs += func
                } else {
                  throw new Error(s"Type mismatch: expected $expectedType but found ${func.funcType}.")
                }
              case _ => throw new Error(s"Import mismatch: expected a function but found $exp.")
          case Import.Global(_, _, GlobalType(tpe, mut)) =>
            exp match
              case ExternalValue.Global(addr) =>
                val glob = from.globalAddrs(addr)
                // TODO: check mutability (=> add mut to GlobalInstance)
                globs += glob
              case _ => throw new Error(s"Import mismatch: expected a global but found $exp.")
          case Import.Table(_, _, tpe) =>
            exp match
              case ExternalValue.Table(addr) =>
                val tab = from.tableAddrs(addr)
                // TODO: check table type
                tabs += tab
              case _ => throw new Error(s"Import mismatch: expected a table but found $exp.")
          case Import.Memory(_, _, tpe) =>
            exp match
              case ExternalValue.Memory(addr) =>
                val mem = from.memoryAddrs(addr)
                // TODO: check memory type
                mems += mem
              case _ => throw new Error(s"Import mismatch: expected a memory but found $exp.")
      }
    }

    (funcs.result(), globs.result(), tabs.result(), mems.result())

  private var initialized: Boolean = false
  def initializeThis(): Unit =
    if (!initialized) {
      globals.putNew(globalTableIndex)
      initialized = true
    }

  // we assume a valid module here
  def initializeModule(module: Module, imports: Imports = mutable.Map.empty): ModuleInstance = external {
    initializeThis()

    val modInst = new ModuleInstance
    var loc = InstLoc.InInit(modInst, 0)
    // compute the initilization values for globals
    val (funcImports, globImports, tabImports, memImpors) = resolveImports(module, imports)
    modInst.globalAddrs = globImports
    val globValues = module.globals.map { glob =>
      val id = BlockId(glob)
      loc = modInst.registerBlockSizes(id, loc, glob.init)
      evalInstructionSequence(id, glob.init, modInst)
    }
    // in the current swam version reference vectors are already provided via the elem fields of the module
    // -> we don't have to compute anything here for now

    // allocate structures for the new module
    // types
    modInst.functionTypes = module.types

    // functions
    val funcImportsSize = funcImports.size
    funcImports.foreach(modInst.addFunction)
    module.funcs.view.zipWithIndex.map { (func, ix) =>
      FunctionInstance.Wasm(modInst, funcImportsSize + ix, func, module.types(func.tpe))
    }.foreach(modInst.addFunction)

    // globals
    modInst.globalAddrs = modInst.globalAddrs :++ module.globals.zip(globValues).map {
      case (Global(GlobalType(tpe, _), _), value) =>
        val globalAddr = GlobalAddr(globCount)
        globCount += 1
        writeGlobalValue(globalAddr, value)
        globalAddr
    }
    // tables
    modInst.tableAddrs = tabImports ++ module.tables.map {
      case TableType(_, Limits(min, max)) =>
        val tabAddr = TableAddr(tabCount)
        funTable.putNew(tabAddr)
        tabCount += 1
        tabAddr
    }
    // memory
    modInst.memoryAddrs = memImpors ++ module.mems.map {
      case MemType(Limits(min, max)) =>
        val initSize = valToSize(i32ops.integerLit(min))
        val sizeLimit = max.map(i => valToSize(i32ops.integerLit(i)))
        val memAddr = MemoryAddr(memCount)
        memory.putNew(memAddr, initSize, sizeLimit)
        memCount += 1
        memAddr
    }
    // data
    modInst.data = module.data.map {
      case Data(_, _, init) => DataInstance(init.toByteVector)
    }
    // we don't need elems currently
    // exports
    modInst.exports = module.exports.map {
      case Export(fieldName, kind, index) =>
        kind match {
          case ExternalKind.Function => (fieldName, ExternalValue.Function(index))
          case ExternalKind.Global => (fieldName, ExternalValue.Global(index))
          case ExternalKind.Memory => (fieldName, ExternalValue.Memory(index))
          case ExternalKind.Table => (fieldName, ExternalValue.Table(index))
        }
    }

    // initialize tables and memories
    // memory
    module.data.zipWithIndex.foreach {
      case (data@Data(memIdx, off, init), i) =>
        assert(memIdx == 0)
        val id = BlockId(data)
        loc = modInst.registerBlockSizes(id, loc, off)
        val base = evalInstructionSequence(id, off, modInst)
        val bytes = init.toByteVector.toIterable
        callFrame.withNew(FrameData(1, modInst), Iterable.empty) {
          bytes.zipWithIndex.foreach { (byte, byteIdx) =>
            stack.push(base)
            stack.push(num.evalNumeric(i32.Const(byte.toInt)))
            store(i32.Store8(0, byteIdx))
          }
        }
      // in case we want to use memory.init here:
      //stack.push(num.evalNumeric(i32.Const(0)))
      //stack.push(num.evalNumeric(i32.Const((init.size / 8).toInt))) //is it ok to convert long to int here?
      //memoryInit(i)
      // memoryDrop(i) for the current wasm version
    }
    // tables
    module.elem.zipWithIndex.foreach {
      case (elem@Elem(tableIdx, off, init), i) =>
        val id = BlockId(elem)
        loc = modInst.registerBlockSizes(id, loc, off)
        val base = evalInstructionSequence(id, off, modInst)
        init.zipWithIndex.foreach { (funcIx, i) =>
          stack.push(base)
          stack.push(num.evalNumeric(i32.Const(i)))
          stack.push(num.evalNumeric(i32.Add)) // adds index to base
          val idx = stack.popOrAbort() // stack is empty
          val funV = functionOps.funValue(modInst.functions(funcIx)) // funcIx is valid due to validation
          funTable.set(TableAddr(modInst.tableAddrs(tableIdx).addr), valueToFuncIx(idx), funV)
          // TODO add failure conditions for table writing
        }
    }

    // invoke the start function
    module.start.foreach {
      funcIdx =>
        val func = modInst.functions(funcIdx)
        func match
          case FunctionInstance.Wasm(mod, ix, func, funcType) =>
            val frameData = FrameData(funcType.t.size, mod)
            val vars = func.locals.map(num.defaultValue)
            labelStack.withNew(stack.withNewFrame(0)(callFrame.withNew(frameData, vars.view.zipWithIndex.map(_.swap)) {
              enterFunction(FuncId(mod, ix), func, funcType)
            }))
          case _: FunctionInstance.Host => ??? // TODO: is it allowed to use host functions as start function?
    }
    //stack.ifEmpty({}, {throw IllegalStateException("Stack is not empty after module initialization.")})
    modInst
  }

