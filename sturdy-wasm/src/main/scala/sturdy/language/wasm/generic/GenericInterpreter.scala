package sturdy.language.wasm.generic

import sturdy.data.unit
import sturdy.effect.ComputationJoiner
import sturdy.effect.Effectful
import sturdy.effect.callframe.DecidableMutableCallFrame
import sturdy.effect.except.Except
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.{Soundness, fix, IsSound}
import sturdy.effect.operandstack.OperandStack
import sturdy.effect.bytememory.Memory
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.operandstack.DecidableOperandStack
import sturdy.effect.symboltable.DecidableSymbolTable
import sturdy.effect.symboltable.SymbolTable
import sturdy.effect.symboltable.JoinedSymbolTable
import sturdy.values.Finite
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


type GenericEffects[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, MayJoin[_]] =
  DecidableOperandStack[V]
    with Memory[MemoryAddr, Addr, Bytes, Size, MayJoin]  
    with Globals[V]                                      
    with SymbolTable[TableAddr, FuncIx, FunV, MayJoin]   
    with DecidableMutableCallFrame[FrameData, Int, V]
    with Except[WasmException[V], ExcV, MayJoin]         
    with Failure

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

trait GenericInterpreter[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, MayJoin[_], Effects <: GenericEffects[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, MayJoin]]
  (val effects: Effects)
  (using MayJoin[Unit], MayJoin[V])
  extends fix.Fixpoint[FixIn, FixOut[V]]:

  import effects.*
  private given Failure = effects
  val stack: DecidableOperandStack[V] = effects

  val wasmOps: WasmOps[V, Addr, Bytes, Size, ExcV, FuncIx, FunV, MayJoin]
  import wasmOps.*
  import branchOps.*
  import specialOps.*
  import exceptOps.*

  lazy val num = new GenericInterpreterNumerics[V, MayJoin](effects, wasmOps)

  val labelStack = new LabelStack
  private var memCount = 0
  private var tabCount = 0
  private var globCount = 0

  // add empty table at addr 0 for global variables
  assert(TableAddr(tabCount) == globalTableIndex)
  addEmptyTable(globalTableIndex)
  tabCount += 1


  inline private def fail(k: FailureKind, what: String) = effects.fail(k, s"$what in $module")
  //inline private def stackPop: V = stack.pop().getOrElse(throw IllegalStateException("pop on empty stack"))
  //inline private def stackPeek: V = stack.peek().getOrElse(throw IllegalStateException("peek on empty stack"))

  def module: ModuleInstance = getFrameData.module

  def evalVarInst(inst: VarInst): Unit = inst match
    case LocalGet(ix) =>
      val v = getLocal(ix).getOrElse(fail(UnboundLocal, ix.toString))
      stack.push(v)
    case LocalSet(ix) =>
      val v = stack.popOrFail()
      setLocal(ix, v).getOrElse(fail(UnboundLocal, ix.toString))
    case LocalTee(ix) =>
      val v = stack.peekOrFail()
      setLocal(ix, v).getOrElse(fail(UnboundLocal, ix.toString))
    case GlobalGet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val global = readGlobal(globalIdx)
      stack.push(global)
    case GlobalSet(globalIx) =>
      val globalIdx = module.globalAddrs.lift(globalIx).getOrElse(fail(UnboundGlobal, globalIx.toString))
      val v = stack.popOrFail()
      val _ = readGlobal(globalIdx) // fails when global is undefined
      writeGlobal(globalIdx, v)

  def evalMemoryInst(inst: Inst): Unit = inst match
    case i: LoadInst => load(i)
    case i: LoadNInst => load(i)

    case i: StoreInst => store(i)
    case i: StoreNInst => store(i)

    case MemorySize =>
      val memIdx = memoryIndex
      stack.push(sizeToVal(memSize(memIdx)))
    case MemoryGrow =>
      val delta = valToSize(stack.popOrFail())
      val memIdx = memoryIndex
      val res = memGrow(memIdx, delta).option
        (num.evalNumeric(i32.Const(0xFFFFFFFF))) // 0xFFFFFFFF ~= -1
        {sizeToVal(_)}
      stack.push(res)
    case _ => throw new IllegalArgumentException(s"Expected memory instruction, but got $inst")

  def load(inst: LoadInst | LoadNInst): Unit =
    // add offset to base address (which is already on the stack)
    stack.push(intOps.integerLit(inst.offset))
    addWithOverflowCheck
    val effectiveAddr = stack.popOrFail()
    val addr = valueToAddr(effectiveAddr)

    val memIdx = memoryIndex
    val byteSize = getBytesToRead(inst)
    memRead(memIdx,addr,byteSize).option
      (fail(MemoryAccessOutOfBounds, s"Cannot read $byteSize bytes at address $addr in current memory."))
      {(b: Bytes) =>
        val v = decode(b, SomeCC(inst, false))
        stack.push(v)}

  def store(inst: StoreInst | StoreNInst): Unit =
    val v = stack.popOrFail()
    val bytes = encode(v, SomeCC(inst, false))

    // add offset to base address (which is already on the stack)
    stack.push(intOps.integerLit(inst.offset))
    val addr = valueToAddr(num.evalNumeric(i32.Add))

    val memIdx = memoryIndex
    memStore(memIdx, addr, bytes).getOrElse(
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

  val globalTableIndex: TableAddr =
    TableAddr(0)

  def getGlobalValue(ga: GlobalAddr): V =
    readGlobal(ga)

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
        val v = stack.popOrFail()
        stack.push(num.evalMiscop(op, v))
      case Drop => stack.popOrFail()
      case Select =>
        val isZero = num.evalNumeric(i32.Eqz)
        boolBranch[Unit](isZero) {
          // v == 0: else branch
          val (_, v2) = stack.pop2OrFail()
          stack.push(v2)
        } {
          stack.popOrFail()
        }
      case _ => throw new IllegalArgumentException(s"Unexpected instruction $inst")

  def evalControlInst(inst: Inst, loc: InstLoc)(using Fixed): Unit = inst match
    case Nop => // nothing
    case Unreachable => fail(UnreachableInstruction, inst.toString)
    case b@Block(bt, insts) =>
      label(BlockId(b), paramsArity(bt), returnArity(bt), insts, None)
    case l@Loop(bt, insts) =>
      val pt = paramsArity(bt)
      label(BlockId(l), pt, pt, insts, Some((l, loc)))
    case ifInst@If(bt, thnInsts, elsInsts) =>
      val isZero = num.evalNumeric(i32.Eqz)
      val rt = returnArity(bt)
      boolBranch[Unit](isZero) {
        // v == 0: else branch
        label(BlockId(ifInst -> false), paramsArity(bt), rt, elsInsts, None)
      } {
        label(BlockId(ifInst -> true), paramsArity(bt), rt, thnInsts, None)
      }
    case Br(labelIndex) =>
      branch(labelIndex)
    case BrIf(labelIndex) =>
      val isZero = num.evalNumeric(i32.Eqz)
      val res = boolBranch[Unit](isZero) {
        // v == 0: else branch
        // do nothing
      } {
        branch(labelIndex)
      }
      res
    case BrTable(labels, defaultLabel) =>
      val ix = stack.popOrFail()
      indexLookup(ix, labels).orElseAndThen(defaultLabel)(branch)
    case Return =>
      val operands = stack.popNOrFail(getFrameData.returnArity)
      throws(WasmException.Return(operands))
    case Call(funcIx) =>
      val func = module.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
      invoke(func)
    case CallIndirect(typeIx) =>
      val ftExpected = module.functionTypes(typeIx)
      val funcIx = stack.popOrFail()
      tableGet(tableIndex, valueToFuncIx(funcIx)).orElseAndThen(fail(UnboundFunctionIndex, funcIx.toString)) { func =>
        invokeIndirect(func, ftExpected, funcIx)
      }
    case _ => throw new IllegalArgumentException(s"Expected control instruction, but got $inst")

  def branch(labelIndex: LabelIdx): Unit =
    val returnArity: Int = labelStack.lookupLabel(labelIndex)
    val operands = stack.popNOrFail(returnArity)
    throws(WasmException.Jump(labelIndex, operands))

  /* stack before label-call:  A p0 ... pn (n = params arity)
 * finish without exception: A r0 ... rm (m = return arity) => nothing to do
 * catch Jump(l0, op0, ..., opm)
 *   - this block is the jump target
 *   - stack: A g0 ... gk needs to become A op0 ... opm
 *   - we don't know k, so we need to remember size of stack A
 * catch Jump(li, op0, ..., opl) i != 0 and Return(op0, ..., opl)
 *   - jump target is further out
 *   - stack: A g0 ... gok needs to become A
 *   - we don't know k, so we need to remember size of stack A
 */
  def label(block: BlockId, paramsArity: Int, returnArity: Int, insts: Iterable[Inst], branchTarget: Option[(Inst, InstLoc)])(using Fixed): Unit =
    stack.withFreshOperandFrame {
      tryCatch {
        labelStack.pushLabel(returnArity)
        try
          val modInst = module
          for ((inst, ix) <- insts.zipWithIndex) {
            val loc = modInst.blockInstLocs((block, ix))
            eval(inst, loc)
          }
        finally labelStack.popLabel()
      } { ex =>
        stack.clearCurrentOperandFrame()
        ex match {
          case WasmException.Jump(labelIndex, operands) =>
            if (labelIndex == 0) {
              stack.pushN(operands)
              for ((i,loc) <- branchTarget)
                eval(i, loc)
            } else {
              throws(WasmException.Jump(labelIndex - 1, operands))
            }
          case _: WasmException.Return[V] =>
            throws(ex)
        }
      }
    }

  def invoke(fun: FunctionInstance)(using Fixed): Unit =
    fun match
      case FunctionInstance.Wasm(mod, ix, func, funcType) =>
        val args = stack.popNOrFail(funcType.params.size)
        val frameData = FrameData(funcType.t.size, mod)
        val vars = args.view ++ func.locals.map(num.defaultValue)
        labelStack.withFresh(stack.withFreshOperandFrame(inNewFrame(frameData, vars.view.zipWithIndex.map(_.swap)) {
          enterFunction(FuncId(mod, ix), func, funcType)
        }))
      case FunctionInstance.Host(hostFunc) =>
        val args = stack.popNOrFail(hostFunc.funcType.params.size)
        val res = invokeHostFunction(hostFunc, args)
        val expectedSize = hostFunc.funcType.t.size
        if (res.length != expectedSize) {
          throw new Error(s"Host function returned the wrong number of results: expected $expectedSize, but got ${res.length}.")
        }
        stack.pushN(res)

  def enterFunction_open(id: FuncId, func: Func, funcType: FuncType)(using Fixed): List[V] =
    val returnN = funcType.t.size
    tryCatch {
      label(BlockId(id), 0, returnN, func.body, None)
    } { ex =>
      stack.clearCurrentOperandFrame()
      ex match {
        case WasmException.Return(operands) =>
          stack.pushN(operands)
        case WasmException.Jump(_, _) =>
          fail(InvalidModule, s"Tried to jump through a function boundary.")
      }
    }
    stack.peekNOrFail(returnN)

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

  private lazy val fixed: Fixed = fixpoint {
    case FixIn.Eval(inst, loc) =>
      eval_open(inst, loc)
      FixOut.Eval()
    case FixIn.EnterWasmFunction(id, func, funcType) =>
      FixOut.ExitWasmFunction(enterFunction_open(id, func, funcType))
  }
  inline def external[A](f: Fixed ?=> A): A = f(using fixed)

  def invokeExported(modInst: ModuleInstance, funcName: String, args: List[V]): List[V] = external {
    stack.withFreshOperandStack {
      modInst.exports.find(entry => entry._1 == funcName) match
        case Some((_, ExternalValue.Function(funcIx))) =>
          val fun = modInst.functions.lift(funcIx).getOrElse(fail(UnboundFunctionIndex, funcIx.toString))
          val paramTys = fun.funcType.params
          if (paramTys.length != args.length)
            throw new Error(s"Wrong number of arguments in external invocation. Expected ${paramTys.length} but got ${args.length}.")
          // paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
          val rtLength = fun.funcType.t.length
          stack.pushN(args)
          inNewFrame(FrameData(0, modInst), Iterable.empty) {
            eval(Call(funcIx), InstLoc.InvokeExported(modInst, funcName))
          }
          stack.popNOrFail(rtLength)
        case _ => throw new Error(s"Function with name $funcName was not found in module's exports.")
    }
  }


  private def returnArity(bt: BlockType): Int =
    val returnArity = bt.arity(module.functionTypes)
    if (returnArity < 0)
      fail(UnboundFunctionType, bt.toString)
    else
      returnArity

  private def paramsArity(bt: BlockType): Int =
    bt.params(module.functionTypes) match
      case Some(params) => params.size
      case None => fail(UnboundFunctionType, bt.toString)




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
    labelStack.withFresh(withFreshOperandStack(inNewFrame(frameData, Iterable.empty){
      for ((inst, ix) <- insts.zipWithIndex) {
        val loc = mod.blockInstLocs((block, ix))
        eval(inst, loc)
      }
      stack.popOrFail()
    }))

  def addWithOverflowCheck =
    val v1 = stack.popOrFail()
    val v2 = stack.popOrFail()
    val res = intOps.add(v1,v2)
    val cmp = unsignedCompareOps.ltUnsigned(res,v1)
    boolBranch[Unit](cmp) {
      fail(IntegerOverflow, s"$v1 + $v2")
    } {
      stack.push(res)
    }
  
  def resolveImports(module: Module, imports: Imports):
    (Vector[FunctionInstance], Vector[GlobalAddr], Vector[TableAddr], Vector[MemoryAddr]) =
    val funcs: VectorBuilder[FunctionInstance] = VectorBuilder()
    val globs: VectorBuilder[GlobalAddr] = VectorBuilder()
    val tabs: VectorBuilder[TableAddr] = VectorBuilder()
    val mems: VectorBuilder[MemoryAddr] = VectorBuilder()

    module.imports.foreach { imp =>
      // handle host functions
      if (imp.moduleName == "wasi_snapshot_preview1") {
        imp match
          case Import.Function(_,funcName, funcType) =>
            val hf = HostFunction.nameToHostFunction(funcName)
            if (hf.funcType != module.types(funcType))
              throw new Error(s"Importing host function with wrong type: expected ${hf.funcType}, but got ${module.types(funcType)}")
            funcs += FunctionInstance.Host(hf)
          case _ => throw new Error(s"Import from runtime: expected a function, but got ${imp}.")
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

  // we assume a valid module here
  def initializeModule(module: Module, imports: Imports = mutable.Map.empty): ModuleInstance = external {
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
        writeGlobal(globalAddr, value)
        globalAddr
    }
    // tables
    modInst.tableAddrs = tabImports ++ module.tables.map {
      case TableType(_, Limits(min, max)) =>
        val tabAddr = TableAddr(tabCount)
        addEmptyTable(tabAddr)
        tabCount += 1
        tabAddr
    }
    // memory
    modInst.memoryAddrs = memImpors ++ module.mems.map {
      case MemType(Limits(min, max)) =>
        val initSize = valToSize(intOps.integerLit(min))
        val sizeLimit = max.map(i => valToSize(intOps.integerLit(i)))
        val memAddr = MemoryAddr(memCount)
        addEmptyMemory(memAddr, initSize, sizeLimit)
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
        inNewFrame(FrameData(1, modInst), Iterable.empty) {
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
          val idx = stack.popOrFail() // stack is empty
          val funV = functionOps.funValue(modInst.functions(funcIx)) // funcIx is valid due to validation
          tableSet(TableAddr(modInst.tableAddrs(tableIdx).addr), valueToFuncIx(idx), funV)
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
            labelStack.withFresh(stack.withFreshOperandFrame(inNewFrame(frameData, vars.view.zipWithIndex.map(_.swap)) {
              val res = enterFunction(FuncId(mod, ix), func, funcType)
              //              println(s"invoke exported $funcName = $res should have $rtLength values")
              //              println(func)
            }))
          case _: FunctionInstance.Host => ??? // TODO: is it allowed to use host functions as start function?
    }
    //stack.ifEmpty({}, {throw IllegalStateException("Stack is not empty after module initialization.")})
    modInst
  }

